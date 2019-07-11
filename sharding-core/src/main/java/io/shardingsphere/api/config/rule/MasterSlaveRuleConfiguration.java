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

import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

/**
 * 主从规则配置
 * Master-slave rule configuration.
 * 
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class MasterSlaveRuleConfiguration implements RuleConfiguration {

    /**
     * 名称
     */
    private String name;

    /**
     * 主库名称
     */
    private String masterDataSourceName;

    /**
     * 从库名称（多个）
     */
    private Collection<String> slaveDataSourceNames;

    /**
     * 主从负载均衡算法
     */
    private MasterSlaveLoadBalanceAlgorithm loadBalanceAlgorithm;
}
