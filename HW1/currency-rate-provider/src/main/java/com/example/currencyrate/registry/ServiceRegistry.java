package com.example.currencyrate.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles automatic service registration in Zookeeper.
 * Creates ephemeral nodes with service metadata and manages reconnection.
 */
@Component
public class ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
    
    private final CuratorFramework curatorFramework;
    private final ObjectMapper objectMapper;
    
    @Value("${server.port:9090}")
    private int serverPort;
    
    @Value("${service.registry.path:/services/currency-rate-provider}")
    private String serviceRegistryPath;
    
    @Value("${service.host:localhost}")
    private String serviceHost;
    
    private String instanceId;
    private String instancePath;
    private String host;

    public ServiceRegistry(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initialize() {
        try {
            this.instanceId = "instance-" + UUID.randomUUID().toString();
            this.host = serviceHost;
            
            log.info("Initializing service registry for instance: {} with host: {}", instanceId, host);
            
            addConnectionStateListener();
            registerService();
            
            log.info("Service registry initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize service registry", e);
            throw new RuntimeException("Service registry initialization failed", e);
        }
    }

    private void registerService() {
        try {
            ensureParentPathsExist();
            this.instancePath = serviceRegistryPath + "/instances/" + instanceId;
            
            Map<String, Object> metadata = createServiceMetadata();
            String metadataJson = objectMapper.writeValueAsString(metadata);
            
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(instancePath, metadataJson.getBytes());
            
            log.info("Service registered successfully at path: {} with metadata: {}", 
                    instancePath, metadataJson);
        } catch (Exception e) {
            log.error("Failed to register service in Zookeeper", e);
            throw new RuntimeException("Service registration failed", e);
        }
    }

    private void ensureParentPathsExist() throws Exception {
        if (curatorFramework.checkExists().forPath(serviceRegistryPath) == null) {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(serviceRegistryPath);
            log.info("Created service root path: {}", serviceRegistryPath);
        }
        
        String instancesPath = serviceRegistryPath + "/instances";
        if (curatorFramework.checkExists().forPath(instancesPath) == null) {
            curatorFramework.create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(instancesPath);
            log.info("Created instances path: {}", instancesPath);
        }
    }

    private Map<String, Object> createServiceMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("host", host);
        metadata.put("port", serverPort);
        metadata.put("instanceId", instanceId);
        metadata.put("registeredAt", Instant.now().toString());
        metadata.put("status", "HEALTHY");
        return metadata;
    }


    private void addConnectionStateListener() {
        ConnectionStateListener listener = new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                log.info("Zookeeper connection state changed to: {}", newState);
                
                switch (newState) {
                    case RECONNECTED:
                        log.info("Reconnected to Zookeeper, re-registering service");
                        try {
                            registerService();
                        } catch (Exception e) {
                            log.error("Failed to re-register service after reconnection", e);
                        }
                        break;
                    case SUSPENDED:
                        log.warn("Zookeeper connection suspended");
                        break;
                    case LOST:
                        log.error("Zookeeper connection lost");
                        break;
                    case CONNECTED:
                        log.info("Connected to Zookeeper");
                        break;
                    case READ_ONLY:
                        log.warn("Zookeeper connection is read-only");
                        break;
                }
            }
        };
        
        curatorFramework.getConnectionStateListenable().addListener(listener);
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (instancePath != null && curatorFramework.checkExists().forPath(instancePath) != null) {
                curatorFramework.delete().forPath(instancePath);
                log.info("Service unregistered successfully from path: {}", instancePath);
            }
        } catch (Exception e) {
            log.error("Failed to unregister service from Zookeeper", e);
        }
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstancePath() {
        return instancePath;
    }
}