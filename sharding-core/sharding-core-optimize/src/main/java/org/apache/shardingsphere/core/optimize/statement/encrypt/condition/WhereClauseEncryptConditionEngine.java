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

package org.apache.shardingsphere.core.optimize.statement.encrypt.condition;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.filler.impl.dml.PredicateUtils;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Encrypt condition engine for where clause.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class WhereClauseEncryptConditionEngine {
    
    private final EncryptRule encryptRule;

    private final ShardingTableMetaData shardingTableMetaData;

    /**
     * Create encrypt conditions.
     *
     * @param sqlStatement SQL statement
     * @return encrypt conditions
     */
    public AndCondition createEncryptConditions(final SQLStatement sqlStatement) {
        Collection<Condition> conditions = new LinkedList<>();
        for (OrPredicateSegment each : sqlStatement.findSQLSegments(OrPredicateSegment.class)) {
            conditions.addAll(createEncryptConditions(each, sqlStatement));
        }
        AndCondition result = new AndCondition();
        result.getConditions().addAll(conditions);
        return result;
    }

    private Collection<Condition> createEncryptConditions(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        Collection<Condition> result = new LinkedList<>();
        Collection<Integer> stopIndexes = new HashSet<>();
        for (AndPredicate each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (stopIndexes.add(predicate.getStopIndex())) {
                    Optional<Condition> condition = createEncryptCondition(predicate, sqlStatement);
                    if (condition.isPresent()) {
                        result.add(condition.get());
                    }
                }
            }
        }
        return result;
    }
    
    private Optional<Condition> createEncryptCondition(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        Optional<String> tableName = PredicateUtils.findTableName(predicateSegment, sqlStatement, shardingTableMetaData);
        if (!tableName.isPresent() || !encryptRule.getEncryptorEngine().getShardingEncryptor(tableName.get(), predicateSegment.getColumn().getName()).isPresent()) {
            return Optional.absent();
        }
        return createEncryptCondition(predicateSegment, new Column(predicateSegment.getColumn().getName(), tableName.get()));
    }

    private Optional<Condition> createEncryptCondition(final PredicateSegment predicateSegment, final Column column) {
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue compareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return isSupportedOperator(compareRightValue.getOperator()) ? PredicateUtils.createCompareCondition(compareRightValue, column, predicateSegment) : Optional.<Condition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return PredicateUtils.createInCondition((PredicateInRightValue) predicateSegment.getRightValue(), column, predicateSegment);
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            throw new SQLParsingException("The SQL clause 'BETWEEN...AND...' is unsupported in encrypt rule.");
        }
        return Optional.absent();
    }

    private boolean isSupportedOperator(final String operator) {
        return "=".equals(operator) || "<>".equals(operator) || "!=".equals(operator);
    }
}
