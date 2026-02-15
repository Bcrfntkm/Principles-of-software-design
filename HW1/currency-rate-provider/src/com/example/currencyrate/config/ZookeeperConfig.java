package com.example.currencyrate.config;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Zookeeper configuration for CuratorFramework client setup.
 */
@Configuration
@ConfigurationProperties(prefix = "zookeeper")
@Data
public class ZookeeperConfig {

    private String connectionString = "localhost:2181";

    private int sessionTimeout = 60000;

    private int connectionTimeout = 15000;

    private int retryBaseSleepTime = 1000;

    private int retryMaxAttempts = 3;

    @Bean(destroyMethod = "close")
    public CuratorFramework curatorFramework() {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(
                retryBaseSleepTime,
                retryMaxAttempts
        );

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        return client;
    }
}