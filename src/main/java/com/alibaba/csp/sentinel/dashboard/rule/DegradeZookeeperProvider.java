/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import java.util.ArrayList;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guifei.qin
 * @Classname DegradeZookeeperProvider
 * @Description 从配置中心zk获取降级规则
 * @Date 2019-07-08
 * @Version V1.0
 */
@Component("degradeRuleZookeeperProvider")
public class DegradeZookeeperProvider implements DynamicRuleProvider<List<DegradeRuleEntity>> {

    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    private Converter<String, List<DegradeRuleEntity>> converter;
    @Autowired
    private ZookeeperSentinelConfig zkConfig;

    @Override
    public List<DegradeRuleEntity> getRules(String appName) throws Exception {
        String zkPath = zkConfig.getDegradeRulePath(appName);
        byte[] bytes = zkClient.getData()
                               .forPath(zkPath);
        if (null == bytes || bytes.length == 0) {
            return new ArrayList<>();
        }
        String s = new String(bytes);

        return converter.convert(s);
    }
}