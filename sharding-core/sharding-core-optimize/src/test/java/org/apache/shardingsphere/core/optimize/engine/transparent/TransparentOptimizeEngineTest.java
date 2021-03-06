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

package org.apache.shardingsphere.core.optimize.engine.transparent;

import org.apache.shardingsphere.core.optimize.statement.transparent.TransparentOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DDLStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TransparentOptimizeEngineTest {
    
    @Test
    public void assertOptimize() {
        SQLStatement sqlStatement = new DDLStatement();
        TransparentOptimizedStatement actual = new TransparentOptimizeEngine(sqlStatement).optimize();
        assertThat(actual.getSQLStatement(), is(sqlStatement));
    }
}
