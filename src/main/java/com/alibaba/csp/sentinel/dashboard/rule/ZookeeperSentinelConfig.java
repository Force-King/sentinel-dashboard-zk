package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author CleverApe
 * @Classname ZookeeperSentinelConfig
 * @Description Sentinel 注册zk配置中心
 * @Date 2019-07-06 11:19
 * @Version V1.0
 */
@Component
public class ZookeeperSentinelConfig {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperSentinelConfig.class);

    @Value("${zk.address}")
    private String zkAddress;
    @Value("${zk.sentinel.path}")
    private String zkPath;
    @Value("${zk.sentinel.appName}")
    private String appName;

    private final String FLOW_PATH = "/flow";
    private final String DEGRADE_PATH = "/degrade";

    @PostConstruct
    public void loadRules() {
        //限流Flow
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<>(zkAddress, zkPath + appName,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        
        //降级Degrade
        String degradePath = zkPath + appName + DEGRADE_PATH;
        Converter<String, List<DegradeRule>> degradeRules = source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
        });
        ReadableDataSource<String, List<DegradeRule>> zkDataSourceDegrade = new ZookeeperDataSource<>(zkAddress, degradePath, degradeRules);
        DegradeRuleManager.register2Property(zkDataSourceDegrade.getProperty());
        logger.info("----------------- Sentinel DataSource Zookeeper Init Success -------------------");
    }


    public String getFlowRulePath(String appName) {
        if (appName.startsWith("/")) {
            return zkPath+appName+FLOW_PATH;
        } else {
            return zkPath + "/" + appName + FLOW_PATH;
        }
    }

    public String getDegradeRulePath(String appName) {
        if (appName.startsWith("/")) {
            return zkPath+appName+DEGRADE_PATH;
        } else {
            return zkPath + "/" + appName + DEGRADE_PATH;
        }
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public String getZkPath() {
        return zkPath;
    }

}
