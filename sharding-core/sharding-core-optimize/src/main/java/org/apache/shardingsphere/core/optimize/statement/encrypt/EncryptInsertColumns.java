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

package org.apache.shardingsphere.core.optimize.statement.encrypt;

import lombok.Getter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.statement.InsertColumns;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Insert columns for encrypt.
 *
 * @author zhangliang
 */
public final class EncryptInsertColumns implements InsertColumns {
    
    private final Collection<String> assistedQueryColumnNames;
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public EncryptInsertColumns(final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        assistedQueryColumnNames = encryptRule.getEncryptorEngine().getAssistedQueryColumns(tableName);
        regularColumnNames = insertStatement.getColumnNames().isEmpty() ? getRegularColumnNamesFromMetaData(shardingTableMetaData, tableName) : insertStatement.getColumnNames();
    }
    
    private Collection<String> getRegularColumnNamesFromMetaData(final ShardingTableMetaData shardingTableMetaData, final String tableName) {
        Collection<String> allColumnNames = shardingTableMetaData.getAllColumnNames(tableName);
        Collection<String> result = new LinkedHashSet<>(allColumnNames.size() - assistedQueryColumnNames.size());
        for (String each : allColumnNames) {
            if (!assistedQueryColumnNames.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    @Override
    public Collection<String> getAllColumnNames() {
        Collection<String> result = new LinkedHashSet<>(regularColumnNames.size() + assistedQueryColumnNames.size());
        result.addAll(regularColumnNames);
        result.addAll(assistedQueryColumnNames);
        return result;
    }
}
