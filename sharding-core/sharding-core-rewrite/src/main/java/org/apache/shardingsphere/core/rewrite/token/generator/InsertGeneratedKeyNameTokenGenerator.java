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
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.InsertRewriteStatement;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertGeneratedKeyNameToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert generated key name token generator.
 *
 * @author panjuan
 */
public final class InsertGeneratedKeyNameTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<InsertGeneratedKeyNameToken> generateSQLToken(
            final RewriteStatement rewriteStatement, final ParameterBuilder parameterBuilder, final ShardingRule shardingRule, final boolean isQueryWithCipherColumn) {
        Optional<InsertColumnsSegment> insertColumnsSegment = rewriteStatement.getOptimizedStatement().getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!insertColumnsSegment.isPresent() || insertColumnsSegment.get().getColumns().isEmpty()) {
            return Optional.absent();
        }
        if (rewriteStatement.getOptimizedStatement() instanceof InsertOptimizedStatement) {
            return createInsertGeneratedKeyToken((InsertRewriteStatement) rewriteStatement, insertColumnsSegment.get(), shardingRule);
        }
        return Optional.absent();
    }
    
    private Optional<InsertGeneratedKeyNameToken> createInsertGeneratedKeyToken(
            final InsertRewriteStatement rewriteStatement, final InsertColumnsSegment segment, final ShardingRule shardingRule) {
        String tableName = rewriteStatement.getOptimizedStatement().getTables().getSingleTableName();
        Optional<String> generatedKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        return generatedKeyColumnName.isPresent() && rewriteStatement.getGeneratedKey().isPresent() && rewriteStatement.getGeneratedKey().get().isGenerated()
                ? Optional.of(new InsertGeneratedKeyNameToken(segment.getStopIndex(), generatedKeyColumnName.get(), isToAddCloseParenthesis(tableName, segment, shardingRule)))
                : Optional.<InsertGeneratedKeyNameToken>absent();
    }
    
    private boolean isToAddCloseParenthesis(final String tableName, final InsertColumnsSegment segment, final ShardingRule shardingRule) {
        return segment.getColumns().isEmpty() && 0 == shardingRule.getEncryptRule().getAssistedQueryAndPlainColumns(tableName).size();
    }
}
