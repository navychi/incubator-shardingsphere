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

package org.apache.shardingsphere.core.route;

import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BatchRouteUnitTest {
    
    private static final String DATASOUCE_NAME = "ds";
    
    private static final String SQL = "select * from table where id = ?";
    
    @Test
    public void assertGetParameterSets() {
        BatchRouteUnit batchRouteUnit = new BatchRouteUnit(new RouteUnit(DATASOUCE_NAME, new SQLUnit(SQL, Lists.<Object>newArrayList(1))));
        List<List<Object>> actual = batchRouteUnit.getParameterSets();
        assertThat(actual.size(), is(1));
        assertTrue(actual.get(0).isEmpty());
        batchRouteUnit.mapAddBatchCount(0);
        actual = batchRouteUnit.getParameterSets();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).size(), is(1));
        assertThat(actual.get(0).get(0), CoreMatchers.<Object>is(1));
    }
    
    @Test
    public void assertEquals() {
        BatchRouteUnit actual = new BatchRouteUnit(new RouteUnit(DATASOUCE_NAME, new SQLUnit(SQL, Lists.<Object>newArrayList(1))));
        BatchRouteUnit expected = new BatchRouteUnit(new RouteUnit(DATASOUCE_NAME, new SQLUnit(SQL, Lists.<Object>newArrayList(2))));
        assertTrue(expected.equals(actual));
    }
    
    @Test
    public void assertToString() {
        BatchRouteUnit actual = new BatchRouteUnit(new RouteUnit(DATASOUCE_NAME, new SQLUnit(SQL, Lists.<Object>newArrayList(1))));
        assertThat(actual.toString(), is(
            String.format("BatchRouteUnit(routeUnit=RouteUnit(dataSourceName=%s, sqlUnit=SQLUnit(sql=%s, parameters=[%d])), jdbcAndActualAddBatchCallTimesMap={}, actualCallAddBatchTimes=0)",
                DATASOUCE_NAME, SQL, 1)));
    }
}
