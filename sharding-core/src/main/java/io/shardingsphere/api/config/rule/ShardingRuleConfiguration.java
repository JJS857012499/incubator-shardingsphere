/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.api.config.rule;

import io.shardingsphere.api.config.strategy.ShardingStrategyConfiguration;
import io.shardingsphere.core.keygen.KeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 分片规则配置
 * Sharding rule configuration.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@Getter
@Setter
public final class ShardingRuleConfiguration implements RuleConfiguration {

    /**
     * 默认数据源名称
     */
    private String defaultDataSourceName;

    /**
     * 库/表规则配置
     */
    private Collection<TableRuleConfiguration> tableRuleConfigs = new LinkedList<>();

    /**
     * 绑定分组
     */
    private Collection<String> bindingTableGroups = new LinkedList<>();

    /**
     * 广播表
     */
    private Collection<String> broadcastTables = new LinkedList<>();

    /**
     * 默认数据库分片策略配置
     */
    private ShardingStrategyConfiguration defaultDatabaseShardingStrategyConfig;

    /**
     * 默认表分片策略配置
     */
    private ShardingStrategyConfiguration defaultTableShardingStrategyConfig;

    /**
     * 默认主键生成器
     */
    private KeyGenerator defaultKeyGenerator;

    /**
     * 主从配置
     */
    private Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
}
