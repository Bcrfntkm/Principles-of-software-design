package com.example.rateprinter.config;

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
public class ZookeeperConfig {
    
    /**
     * Zookeeper connection string (e.g., "localhost:2181,localhost:2182,localhost:2183")
     */
    private String connectionString = "localhost:2181";
    
    /**
     * Session timeout in milliseconds
     */
    private int sessionTimeout = 30000;
    
    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeout = 15000;
    
    /**
     * Maximum number of retry attempts
     */
    private int retryMaxAttempts = 3;
    
    /**
     * Base sleep time between retries in milliseconds
     */
    private int retryBaseSleepTime = 1000;
    
    /**
     * Creates and configures a CuratorFramework client for Zookeeper operations.
     * The client uses exponential backoff retry policy for connection resilience.
     *
     * @return configured CuratorFramework instance
     */
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
    
    // Getters and setters for configuration properties
    
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