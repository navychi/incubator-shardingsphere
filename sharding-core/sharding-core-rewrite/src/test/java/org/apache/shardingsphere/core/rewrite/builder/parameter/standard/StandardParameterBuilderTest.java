/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.rewrite.builder.parameter.standard;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptOptimizedStatement;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class StandardParameterBuilderTest {
    
    private StandardParameterBuilder parameterBuilder;
    
    @Before
    public void setUp() {
        parameterBuilder = new StandardParameterBuilder(Arrays.<Object>asList(1, 2, 1, 5), createSQLRouteResult());
        parameterBuilder.getAddedIndexAndParameters().putAll(Collections.singletonMap(4, 7));
    }
    
    private SQLRouteResult createSQLRouteResult() {
        Pagination pagination = mock(Pagination.class);
        when(pagination.isHasPagination()).thenReturn(true);
        when(pagination.getOffsetParameterIndex()).thenReturn(Optional.of(2));
        when(pagination.getRowCountParameterIndex()).thenReturn(Optional.of(3));
        when(pagination.getRevisedRowCount(any(ShardingSelectOptimizedStatement.class))).thenReturn(6L);
        ShardingSelectOptimizedStatement shardingStatement = mock(ShardingSelectOptimizedStatement.class);
        when(shardingStatement.getPagination()).thenReturn(pagination);
        EncryptOptimizedStatement encryptStatement = mock(EncryptOptimizedStatement.class);
        SQLRouteResult result = new SQLRouteResult(shardingStatement, encryptStatement, new ShardingConditions(Collections.<ShardingCondition>emptyList()), null);
        result.setRoutingResult(new RoutingResult());
        return result;
    }
    
    @Test
    public void assertGetParameters() {
        assertThat(parameterBuilder.getParameters(), is(Arrays.<Object>asList(1, 2, 0L, 6L, 7)));
        assertThat(parameterBuilder.getParameters(mock(RoutingUnit.class)), is(Arrays.<Object>asList(1, 2, 0L, 6L, 7)));
    }
    
    @Test
    public void assertGetOriginalParameters() {
        assertThat(parameterBuilder.getOriginalParameters(), is(Arrays.<Object>asList(1, 2, 1, 5)));
    }
    
    @Test
    public void assertGetAddedIndexAndParameters() {
        assertThat(parameterBuilder.getAddedIndexAndParameters(), is(Collections.<Integer, Object>singletonMap(4, 7)));
    }
    
    @Test
    public void assertGetReplacedIndexAndParameters() {
        Map<Integer, Object> expected = new LinkedHashMap<>();
        expected.put(2, 0L);
        expected.put(3, 6L);
        assertThat(parameterBuilder.getReplacedIndexAndParameters(), is(expected));
    }
}
