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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.EncryptColumnToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.WhereEncryptColumnToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Where encrypt column token generator.
 *
 * @author panjuan
 */
public final class WhereEncryptColumnTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<EncryptColumnToken> generateSQLTokens(
            final RewriteStatement rewriteStatement, final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        return rewriteStatement.getEncryptConditions().getConditions().isEmpty()
                ? Collections.<EncryptColumnToken>emptyList() : createWhereEncryptColumnTokens(parameterBuilder, encryptRule, isQueryWithCipherColumn, rewriteStatement);
    }
    
    private Collection<EncryptColumnToken> createWhereEncryptColumnTokens(final ParameterBuilder parameterBuilder, final EncryptRule encryptRule, 
                                                                          final boolean isQueryWithCipherColumn, final RewriteStatement rewriteStatement) {
        Collection<EncryptColumnToken> result = new LinkedList<>();
        for (EncryptCondition each : rewriteStatement.getEncryptConditions().getConditions()) {
            result.add(createWhereEncryptColumnToken(parameterBuilder, encryptRule, isQueryWithCipherColumn, each));
        }
        return result;
    }
    
    private WhereEncryptColumnToken createWhereEncryptColumnToken(final ParameterBuilder parameterBuilder, 
                                                                  final EncryptRule encryptRule, final boolean isQueryWithCipherColumn, final EncryptCondition encryptCondition) {
        List<Object> originalValues = encryptCondition.getValues(parameterBuilder.getOriginalParameters());
        if (isQueryWithCipherColumn) {
            return createWhereEncryptColumnToken(parameterBuilder, encryptRule, encryptCondition, originalValues);
        }
        return new WhereEncryptColumnToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(), getPlainColumn(encryptRule, encryptCondition),
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), originalValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private WhereEncryptColumnToken createWhereEncryptColumnToken(final ParameterBuilder parameterBuilder, 
                                                                  final EncryptRule encryptRule, final EncryptCondition encryptCondition, final List<Object> originalValues) {
        String encryptedColumnName = getEncryptedColumnName(encryptRule, encryptCondition);
        List<Object> encryptedValues = getEncryptedValues(encryptRule, encryptCondition, originalValues);
        encryptParameters(encryptCondition.getPositionIndexMap(), encryptedValues, parameterBuilder);
        return new WhereEncryptColumnToken(encryptCondition.getStartIndex(), encryptCondition.getStopIndex(), encryptedColumnName,
                getPositionValues(encryptCondition.getPositionValueMap().keySet(), encryptedValues), encryptCondition.getPositionIndexMap().keySet(), encryptCondition.getOperator());
    }
    
    private String getEncryptedColumnName(final EncryptRule encryptRule, final EncryptCondition encryptCondition) {
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return assistedQueryColumn.isPresent() 
                ? assistedQueryColumn.get() : encryptRule.getCipherColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
    }
    
    private List<Object> getEncryptedValues(final EncryptRule encryptRule, final EncryptCondition encryptCondition, final List<Object> originalValues) {
        Optional<String> assistedQueryColumn = encryptRule.findAssistedQueryColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        return assistedQueryColumn.isPresent() 
                ? encryptRule.getEncryptAssistedQueryValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues) 
                : encryptRule.getEncryptValues(encryptCondition.getTableName(), encryptCondition.getColumnName(), originalValues);
    }
    
    private void encryptParameters(final Map<Integer, Integer> positionIndexes, final List<Object> encryptValues, final ParameterBuilder parameterBuilder) {
        if (!positionIndexes.isEmpty()) {
            for (Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                parameterBuilder.getOriginalParameters().set(entry.getValue(), encryptValues.get(entry.getKey()));
            }
        }
    }
    
    private Map<Integer, Object> getPositionValues(final Collection<Integer> valuePositions, final List<Object> encryptValues) {
        Map<Integer, Object> result = new LinkedHashMap<>();
        for (int each : valuePositions) {
            result.put(each, encryptValues.get(each));
        }
        return result;
    }
    
    private String getPlainColumn(final EncryptRule encryptRule, final EncryptCondition encryptCondition) {
        Optional<String> result = encryptRule.findPlainColumn(encryptCondition.getTableName(), encryptCondition.getColumnName());
        if (result.isPresent()) {
            return result.get();
        }
        throw new ShardingException("Plain column is required.");
    }
}
