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

package org.apache.shardingsphere.core.parse.filler.impl.ddl.column;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.aware.ShardingTableMetaDataAware;
import org.apache.shardingsphere.core.parse.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.AlterTableStatement;

/**
 * Rename column definition filler.
 *
 * @author duhongjun
 */
@Setter
public final class RenameColumnDefinitionFiller implements SQLSegmentFiller<RenameColumnSegment>, ShardingTableMetaDataAware {
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final RenameColumnSegment sqlSegment, final SQLStatement sqlStatement) {
        AlterTableStatement alterTableStatement = (AlterTableStatement) sqlStatement;
        Optional<ColumnDefinitionSegment> oldColumnDefinition = alterTableStatement.findColumnDefinition(sqlSegment.getOldColumnName(), shardingTableMetaData);
        if (!oldColumnDefinition.isPresent()) {
            return;
        }
        oldColumnDefinition.get().setColumnName(sqlSegment.getColumnName());
        alterTableStatement.getModifiedColumnDefinitions().put(sqlSegment.getOldColumnName(), oldColumnDefinition.get());
    }
}
