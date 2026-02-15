package com.example.rateprinter.loadbalancer;

import com.example.rateprinter.discovery.ServiceDiscovery;
import com.example.rateprinter.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RoundRobinLoadBalancer {
    
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);
    
    private final ServiceDiscovery serviceDiscovery;
    

    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public RoundRobinLoadBalancer(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }
    

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
    
    public int getAvailableInstanceCount() {
        return serviceDiscovery.getInstanceCount();
    }
    

    public static class NoAvailableInstancesException extends RuntimeException {
        public NoAvailableInstancesException(String message) {
            super(message);
        }
    }
}