package com.example.rateprinter.loadbalancer;

import com.example.rateprinter.discovery.ServiceDiscovery;
import com.example.rateprinter.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-robin load balancer for distributing requests across service instances.
 */
@Component
public class RoundRobinLoadBalancer {
    
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);
    
    private final ServiceDiscovery serviceDiscovery;
    
    /**
     * Thread-safe counter for round-robin rotation
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public RoundRobinLoadBalancer(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }
    
    /**
     * Returns the next available service instance using round-robin algorithm.
     * 
     * @return the next service instance to use
     * @throws NoAvailableInstancesException if no instances are available
     */
    public ServiceInstance getNextInstance() {
        List<ServiceInstance> instances = serviceDiscovery.getInstances();
        
        if (instances.isEmpty()) {
            logger.error("No service instances available");
            throw new NoAvailableInstancesException("No currency-rate-provider instances are currently available");
        }
        
        int index = Math.abs(currentIndex.getAndIncrement() % instances.size());
        ServiceInstance selectedInstance = instances.get(index);
        
        logger.debug("Selected instance {} (index {}/{}) using round-robin", 
                    selectedInstance.getInstanceId(), index + 1, instances.size());
        
        return selectedInstance;
    }
    
    /**
     * Returns the current number of available instances.
     * 
     * @return count of available instances
     */
    public int getAvailableInstanceCount() {
        return serviceDiscovery.getInstanceCount();
    }
    
    /**
     * Exception thrown when no service instances are available.
     */
    public static class NoAvailableInstancesException extends RuntimeException {
        public NoAvailableInstancesException(String message) {
            super(message);
        }
    }
}