package com.example.rateprinter.model;

import java.util.Objects;

/**
 * Service instance model with connection information and metadata.
 */
public class ServiceInstance {
    
    /**
     * Host address of the service instance (e.g., "192.168.1.100" or "localhost")
     */
    private String host;
    
    /**
     * Port number where the gRPC service is listening
     */
    private int port;
    
    /**
     * Unique identifier for this service instance
     */
    private String instanceId;
    
    /**
     * Timestamp when the instance was registered in Zookeeper
     */
    private String registeredAt;
    
    /**
     * Current status of the instance (e.g., "ACTIVE", "SHUTTING_DOWN")
     */
    private String status;
    
    /**
     * Default constructor
     */
    public ServiceInstance() {
    }
    
    /**
     * All-args constructor
     */
    public ServiceInstance(String host, int port, String instanceId, String registeredAt, String status) {
        this.host = host;
        this.port = port;
        this.instanceId = instanceId;
        this.registeredAt = registeredAt;
        this.status = status;
    }
    
    // Getters and Setters
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    
    public String getRegisteredAt() {
        return registeredAt;
    }
    
    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Returns the full address in the format "host:port" for gRPC channel creation
     */
    public String getAddress() {
        return host + ":" + port;
    }
    
    /**
     * Custom equals implementation based on instanceId for proper instance comparison
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance that = (ServiceInstance) o;
        return Objects.equals(instanceId, that.instanceId);
    }
    
    /**
     * Custom hashCode implementation based on instanceId
     */
    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }
    
    /**
     * String representation for logging purposes
     */
    @Override
    public String toString() {
        return "ServiceInstance{" +
                "instanceId='" + instanceId + '\'' +
                ", address='" + getAddress() + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}