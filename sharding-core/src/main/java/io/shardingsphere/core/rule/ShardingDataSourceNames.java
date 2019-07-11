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

package io.shardingsphere.core.rule;

import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * 分片数据源名称
 * Sharding data source names.
 *
 * <p>Will convert actual data source names to master-slave data source name.</p>
 *
 * @author zhangliang
 */
public final class ShardingDataSourceNames {

    /**
     * 分片规则配置
     */
    private final ShardingRuleConfiguration shardingRuleConfig;

    /**
     * 数据源名称
     */
    @Getter
    private final Collection<String> dataSourceNames;

    public ShardingDataSourceNames(final ShardingRuleConfiguration shardingRuleConfig, final Collection<String> rawDataSourceNames) {
        this.shardingRuleConfig = shardingRuleConfig;
        dataSourceNames = getAllDataSourceNames(rawDataSourceNames);
    }

    private Collection<String> getAllDataSourceNames(final Collection<String> dataSourceNames) {
        Collection<String> result = new LinkedHashSet<>(dataSourceNames);
        for (MasterSlaveRuleConfiguration each : shardingRuleConfig.getMasterSlaveRuleConfigs()) {
            result.remove(each.getMasterDataSourceName());
            result.removeAll(each.getSlaveDataSourceNames());
            result.add(each.getName());
        }
        return result;
    }

    /**
     * 获取默认的数据源名称
     * Get default data source name.
     *
     * @return default data source name
     */
    public String getDefaultDataSourceName() {
        return 1 == dataSourceNames.size() ? dataSourceNames.iterator().next() : shardingRuleConfig.getDefaultDataSourceName();
    }

    /**
     * 获取真实主库的数据源名称（分片数据库中，存在主从数据库）
     * Get raw master data source name.
     *
     * @param dataSourceName data source name
     * @return raw master data source name
     */
    public String getRawMasterDataSourceName(final String dataSourceName) {
        for (MasterSlaveRuleConfiguration each : shardingRuleConfig.getMasterSlaveRuleConfigs()) {
            if (each.getName().equals(dataSourceName)) {
                return each.getMasterDataSourceName();
            }
        }
        return dataSourceName;
    }
}
