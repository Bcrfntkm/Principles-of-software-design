package com.example.rateprinter.discovery;

import com.example.rateprinter.model.ServiceInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Watches Zookeeper for available producer instances and maintains a local cache.
 */
@Component
public class ServiceDiscovery {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    
    private final CuratorFramework curatorFramework;
    private final ObjectMapper objectMapper;
    
    @Value("${service.discovery.path:/services/currency-rate-provider/instances}")
    private String servicePath;
    
    private final CopyOnWriteArrayList<ServiceInstance> instances = new CopyOnWriteArrayList<>();
    
    private PathChildrenCache pathChildrenCache;
    
    public ServiceDiscovery(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        this.objectMapper = new ObjectMapper();
    }
    
    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initializing service discovery for path: {}", servicePath);
            
            if (curatorFramework.checkExists().forPath(servicePath) == null) {
                logger.warn("Service path {} does not exist yet. Waiting for producers to register...", servicePath);
            }
            
            pathChildrenCache = new PathChildrenCache(curatorFramework, servicePath, true);
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    handleInstanceChange(event);
                }
            });
            
            pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            refreshInstances();
            
            logger.info("Service discovery initialized successfully. Found {} instances", instances.size());
            
        } catch (Exception e) {
            logger.error("Failed to initialize service discovery", e);
            throw new RuntimeException("Service discovery initialization failed", e);
        }
    }
    
    /**
     * Handles changes to service instances in Zookeeper.
     * 
     * @param event the change event from Zookeeper
     */
    private void handleInstanceChange(PathChildrenCacheEvent event) {
        try {
            switch (event.getType()) {
                case CHILD_ADDED:
                    logger.info("New service instance detected");
                    refreshInstances();
                    break;
                    
                case CHILD_REMOVED:
                    logger.info("Service instance removed");
                    refreshInstances();
                    break;
                    
                case CHILD_UPDATED:
                    logger.info("Service instance updated");
                    refreshInstances();
                    break;
                    
                case CONNECTION_RECONNECTED:
                    logger.info("Reconnected to Zookeeper, refreshing instances");
                    refreshInstances();
                    break;
                    
                case CONNECTION_LOST:
                    logger.warn("Lost connection to Zookeeper. Using cached instances.");
                    break;
                    
                default:
                    // Ignore other events
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling instance change event", e);
        }
    }
    
    /**
     * Refreshes the local cache of service instances from Zookeeper.
     */
    private void refreshInstances() {
        try {
            List<ServiceInstance> newInstances = new ArrayList<>();
            
            if (curatorFramework.checkExists().forPath(servicePath) == null) {
                logger.warn("Service path {} does not exist", servicePath);
                instances.clear();
                return;
            }
            
            List<String> children = curatorFramework.getChildren().forPath(servicePath);
            
            for (String child : children) {
                try {
                    String instancePath = servicePath + "/" + child;
                    byte[] data = curatorFramework.getData().forPath(instancePath);
                    
                    if (data != null && data.length > 0) {
                        ServiceInstance instance = objectMapper.readValue(data, ServiceInstance.class);
                        if ("HEALTHY".equals(instance.getStatus()) || "ACTIVE".equals(instance.getStatus())) {
                            newInstances.add(instance);
                            logger.debug("Discovered instance: {}", instance);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse instance data for: {}", child, e);
                }
            }
            
            instances.clear();
            instances.addAll(newInstances);
            
            logger.info("Refreshed service instances. Total active instances: {}", instances.size());
            
        } catch (Exception e) {
            logger.error("Failed to refresh service instances", e);
        }
    }
    
    /**
     * Returns the current list of available service instances.
     * 
     * @return thread-safe copy of available instances
     */
    public List<ServiceInstance> getInstances() {
        return new ArrayList<>(instances);
    }
    
    /**
     * Returns the number of available service instances.
     * 
     * @return count of active instances
     */
    public int getInstanceCount() {
        return instances.size();
    }
    
    /**
     * Cleanup method called before bean destruction.
     * Closes the Zookeeper cache and releases resources.
     */
    @PreDestroy
    public void cleanup() {
        try {
            if (pathChildrenCache != null) {
                pathChildrenCache.close();
                logger.info("Service discovery cache closed");
            }
        } catch (Exception e) {
            logger.error("Error closing service discovery cache", e);
        }
    }
}