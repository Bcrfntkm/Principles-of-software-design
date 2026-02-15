package com.example.rateprinter.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "zookeeper")
public class ZookeeperConfig {
    

    private String connectionString = "localhost:2181";
    
    private int sessionTimeout = 30000;
    
    private int connectionTimeout = 15000;
    
    private int retryMaxAttempts = 3;
    
    private int retryBaseSleepTime = 1000;
    
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
    
    
    public String getConnectionString() {
        return connectionString;
    }
    
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public int getSessionTimeout() {
        return sessionTimeout;
    }
    
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }
    
    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }
    
    public int getRetryBaseSleepTime() {
        return retryBaseSleepTime;
    }
    
    public void setRetryBaseSleepTime(int retryBaseSleepTime) {
        this.retryBaseSleepTime = retryBaseSleepTime;
    }
}