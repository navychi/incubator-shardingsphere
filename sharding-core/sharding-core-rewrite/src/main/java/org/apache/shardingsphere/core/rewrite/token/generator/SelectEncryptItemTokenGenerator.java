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
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.SelectEncryptItemToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Select cipher item token generator.
 *
 * @author panjuan
 */
public final class SelectEncryptItemTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<SelectEncryptItemToken> generateSQLTokens(final RewriteStatement rewriteStatement,
                                                                final ParameterBuilder parameterBuilder, final EncryptRule rule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(rewriteStatement.getOptimizedStatement())) {
            return Collections.emptyList();
        }
        return createSelectCipherItemTokens(rule, rewriteStatement.getOptimizedStatement(), isQueryWithCipherColumn);
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        if (!isSelectStatementWithTable(optimizedStatement)) {
            return false;
        }
        Optional<SelectItemsSegment> selectItemsSegment = optimizedStatement.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        return selectItemsSegment.isPresent() && !selectItemsSegment.get().getSelectItems().isEmpty();
    }
    
    private boolean isSelectStatementWithTable(final OptimizedStatement optimizedStatement) {
        return optimizedStatement.getSqlStatement() instanceof SelectStatement && !optimizedStatement.getTables().isEmpty();
    }
    
    private Collection<SelectEncryptItemToken> createSelectCipherItemTokens(final EncryptRule encryptRule, final OptimizedStatement optimizedStatement, final boolean isQueryWithCipherColumn) {
        Collection<SelectEncryptItemToken> result = new LinkedList<>();
        Optional<SelectItemsSegment> selectItemsSegment = optimizedStatement.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        if (!selectItemsSegment.isPresent()) {
            return Collections.emptyList();
        }
        String tableName = optimizedStatement.getTables().getSingleTableName();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        for (SelectItemSegment each : selectItemsSegment.get().getSelectItems()) {
            if (isLogicColumn(each, encryptTable.get())) {
                result.add(createSelectCipherItemToken(each, tableName, encryptRule, isQueryWithCipherColumn));
            }
        }
        return result;
    }
    
    private boolean isLogicColumn(final SelectItemSegment selectItemSegment, final EncryptTable encryptTable) {
        return selectItemSegment instanceof ColumnSelectItemSegment && encryptTable.getLogicColumns().contains(((ColumnSelectItemSegment) selectItemSegment).getName());
    }
    
    private SelectEncryptItemToken createSelectCipherItemToken(final SelectItemSegment selectItemSegment, 
                                                               final String tableName, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        String columnName = ((ColumnSelectItemSegment) selectItemSegment).getName();
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, columnName);
        if (!isQueryWithCipherColumn && plainColumn.isPresent()) {
            return createSelectEncryptItemToken(selectItemSegment, plainColumn.get());
        }
        return createSelectEncryptItemToken(selectItemSegment, encryptRule.getCipherColumn(tableName, columnName));
    }
    
    private SelectEncryptItemToken createSelectEncryptItemToken(final SelectItemSegment selectItemSegment, final String columnName) {
        Optional<TableSegment> owner = ((ColumnSelectItemSegment) selectItemSegment).getOwner();
        if (owner.isPresent()) {
            return new SelectEncryptItemToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), columnName, owner.get().getTableName());
        }
        return new SelectEncryptItemToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), columnName);
    }
}
