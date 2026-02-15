# Testing Guide for Zookeeper Integration

This guide provides comprehensive instructions for testing the Zookeeper-based service discovery and load balancing implementation for the Currency Rate Services.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Testing Scenarios](#testing-scenarios)
- [Verification Steps](#verification-steps)
- [Troubleshooting](#troubleshooting)

## Prerequisites

Before you begin testing, ensure you have the following installed:

### Required Software
1. **Java 17**
   ```bash
   java -version
   # Should output: java version "17.x.x"
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   # Should output: Apache Maven 3.6.x or higher
   ```

3. **Docker and Docker Compose**
   ```bash
   docker --version
   # Should output: Docker version 20.x.x or higher
   
   docker-compose --version
   # Should output: docker-compose version 1.29.x or higher
   ```

### Installation Links
- **Java 17**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/#java17) or [OpenJDK](https://adoptium.net/)
- **Maven**: [Apache Maven](https://maven.apache.org/download.cgi)
- **Docker**: [Docker Desktop](https://www.docker.com/products/docker-desktop)

## Setup Instructions

### 1. Start Zookeeper

Navigate to the project directory and start Zookeeper using Docker Compose:

```bash
cd "BIT/Architechture Design/Principles-of-software-design/HW1"
docker-compose up -d
```

**Verify Zookeeper is running:**
```bash
docker-compose ps
```

Expected output:
```
NAME                 IMAGE          STATUS         PORTS
zookeeper-server     zookeeper:3.9  Up (healthy)   0.0.0.0:2181->2181/tcp
```

**Check Zookeeper logs:**
```bash
docker-compose logs -f zookeeper
```

Look for: `"binding to port 0.0.0.0/0.0.0.0:2181"`

### 2. Build Both Modules

Build the producer (currency-rate-provider):
```bash
cd currency-rate-provider
mvn clean package
cd ..
```

Build the consumer (rate-printer):
```bash
cd rate-printer
mvn clean package
cd ..
```

**Expected output for both:**
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 3. Running Multiple Producer Instances

To test load balancing, you need to run multiple producer instances on different ports.

**Terminal 1 - Producer on port 9090:**
```bash
cd currency-rate-provider
SERVER_PORT=9090 mvn spring-boot:run
```

**Terminal 2 - Producer on port 9091:**
```bash
cd currency-rate-provider
SERVER_PORT=9091 mvn spring-boot:run
```

**Terminal 3 - Producer on port 9092:**
```bash
cd currency-rate-provider
SERVER_PORT=9092 mvn spring-boot:run
```

**Alternative using JAR files:**
```bash
# Terminal 1
cd currency-rate-provider
SERVER_PORT=9090 java -jar target/currency-rate-provider-1.0-SNAPSHOT.jar

# Terminal 2
cd currency-rate-provider
SERVER_PORT=9091 java -jar target/currency-rate-provider-1.0-SNAPSHOT.jar

# Terminal 3
cd currency-rate-provider
SERVER_PORT=9092 java -jar target/currency-rate-provider-1.0-SNAPSHOT.jar
```

## Testing Scenarios

### Scenario 1: Single Producer Test

**Objective:** Verify basic service registration and discovery with one producer.

**Steps:**
1. Start Zookeeper (if not already running)
2. Start one producer instance on port 9090
3. Wait for registration confirmation in logs
4. Start the consumer
5. Observe consumer discovering and connecting to the producer

**Expected Producer Logs:**
```
INFO  c.e.c.registry.ServiceRegistry - Connecting to Zookeeper at localhost:2181
INFO  c.e.c.registry.ServiceRegistry - Successfully registered service instance: instance-xxx
INFO  c.e.c.registry.ServiceRegistry - Service registered at /services/currency-rate-provider/instances/instance-xxx
```

**Expected Consumer Logs:**
```
INFO  c.e.r.discovery.ServiceDiscovery - Connected to Zookeeper at localhost:2181
INFO  c.e.r.discovery.ServiceDiscovery - Discovered 1 service instance(s)
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9090
INFO  c.e.r.service.RatePrinterService - Successfully fetched rate from localhost:9090
```

**Verification:**
- Consumer should successfully print currency rates
- No errors in either producer or consumer logs

---

### Scenario 2: Multiple Producers Test (Load Balancing)

**Objective:** Verify round-robin load balancing across multiple producer instances.

**Steps:**
1. Start Zookeeper
2. Start three producer instances (ports 9090, 9091, 9092)
3. Wait for all three to register (check logs)
4. Start the consumer
5. Observe load distribution in consumer logs

**Expected Producer Logs (for each instance):**
```
INFO  c.e.c.registry.ServiceRegistry - Successfully registered service instance: instance-xxx
```

**Expected Consumer Logs:**
```
INFO  c.e.r.discovery.ServiceDiscovery - Discovered 3 service instance(s)
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9090
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9091
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9092
INFO  c.e.r.loadbalancer.RoundRobinLoadBalancer - Selected instance: localhost:9090
INFO  c.e.r.loadbalancer.RoundRobinLoadBalancer - Selected instance: localhost:9091
INFO  c.e.r.loadbalancer.RoundRobinLoadBalancer - Selected instance: localhost:9092
INFO  c.e.r.loadbalancer.RoundRobinLoadBalancer - Selected instance: localhost:9090
```

**Verification:**
- Consumer should discover all 3 instances
- Requests should be distributed in round-robin fashion: 9090 → 9091 → 9092 → 9090 → ...
- All producers should receive requests

---

### Scenario 3: Dynamic Discovery Test

**Objective:** Verify consumer can discover producers added after consumer startup.

**Steps:**
1. Start Zookeeper
2. Start the consumer FIRST (no producers running)
3. Observe consumer waiting for instances
4. Start producer on port 9090
5. Observe consumer discovering the new instance
6. Start producer on port 9091
7. Observe consumer discovering the second instance

**Expected Consumer Logs (initially):**
```
WARN  c.e.r.discovery.ServiceDiscovery - No service instances available
INFO  c.e.r.discovery.ServiceDiscovery - Waiting for service instances to become available...
```

**After starting first producer:**
```
INFO  c.e.r.discovery.ServiceDiscovery - Service instances changed, refreshing...
INFO  c.e.r.discovery.ServiceDiscovery - Discovered 1 service instance(s)
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9090
```

**After starting second producer:**
```
INFO  c.e.r.discovery.ServiceDiscovery - Service instances changed, refreshing...
INFO  c.e.r.discovery.ServiceDiscovery - Discovered 2 service instance(s)
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9090
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9091
```

**Verification:**
- Consumer should automatically discover new producers without restart
- Load balancing should include newly discovered instances

---

### Scenario 4: Failover Test

**Objective:** Verify consumer continues operating when a producer fails.

**Steps:**
1. Start Zookeeper
2. Start three producers (ports 9090, 9091, 9092)
3. Start the consumer
4. Verify all three instances are discovered
5. Stop one producer (e.g., port 9091) using Ctrl+C
6. Observe consumer detecting the failure
7. Verify consumer continues with remaining instances

**Expected Consumer Logs (after stopping producer):**
```
INFO  c.e.r.discovery.ServiceDiscovery - Service instances changed, refreshing...
INFO  c.e.r.discovery.ServiceDiscovery - Discovered 2 service instance(s)
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9090
INFO  c.e.r.discovery.ServiceDiscovery - Instance: localhost:9092
INFO  c.e.r.loadbalancer.RoundRobinLoadBalancer - Removed failed instance: localhost:9091
```

**Verification:**
- Consumer should detect instance removal within 30 seconds
- Requests should continue with remaining instances
- No errors or exceptions in consumer logs
- Load balancing should work with 2 instances: 9090 → 9092 → 9090 → ...

---

### Scenario 5: Reconnection Test

**Objective:** Verify services reconnect and re-register after Zookeeper restart.

**Steps:**
1. Start Zookeeper
2. Start two producers (ports 9090, 9091)
3. Start the consumer
4. Verify everything is working
5. Stop Zookeeper: `docker-compose stop zookeeper`
6. Observe connection loss in logs
7. Restart Zookeeper: `docker-compose start zookeeper`
8. Observe reconnection and re-registration

**Expected Logs During Zookeeper Downtime:**
```
WARN  o.a.c.framework.imps.CuratorFrameworkImpl - Connection suspended
WARN  c.e.c.registry.ServiceRegistry - Zookeeper connection suspended
```

**Expected Logs After Zookeeper Restart:**
```
INFO  o.a.c.framework.imps.CuratorFrameworkImpl - Connection reconnected
INFO  c.e.c.registry.ServiceRegistry - Zookeeper connection re-established
INFO  c.e.c.registry.ServiceRegistry - Re-registering service instance
INFO  c.e.r.discovery.ServiceDiscovery - Reconnected to Zookeeper, refreshing instances
```

**Verification:**
- Services should automatically reconnect
- Producers should re-register themselves
- Consumer should rediscover all instances
- Normal operation should resume

---

## Verification Steps

### Check Zookeeper Nodes Using zkCli

You can inspect Zookeeper nodes directly using the Zookeeper CLI:

**Access Zookeeper CLI:**
```bash
docker exec -it zookeeper-server zkCli.sh
```

**List service instances:**
```bash
ls /services/currency-rate-provider/instances
```

Expected output:
```
[instance-001, instance-002, instance-003]
```

**Get instance metadata:**
```bash
get /services/currency-rate-provider/instances/instance-001
```

Expected output (JSON format):
```json
{
  "instanceId": "instance-001",
  "host": "localhost",
  "port": 9090,
  "status": "ACTIVE",
  "startTime": "2026-02-15T12:45:00Z"
}
```

**Watch for changes:**
```bash
ls /services/currency-rate-provider/instances watch
```

**Exit zkCli:**
```bash
quit
```

### What to Look For in Logs

#### Producer Logs - Success Indicators:
- ✅ `Successfully registered service instance`
- ✅ `Service registered at /services/currency-rate-provider/instances/...`
- ✅ `gRPC server started on port XXXX`

#### Producer Logs - Warning Signs:
- ⚠️ `Failed to register service`
- ⚠️ `Connection to Zookeeper lost`
- ⚠️ `Port XXXX is already in use`

#### Consumer Logs - Success Indicators:
- ✅ `Discovered N service instance(s)`
- ✅ `Successfully fetched rate from localhost:XXXX`
- ✅ `Selected instance: localhost:XXXX`

#### Consumer Logs - Warning Signs:
- ⚠️ `No service instances available`
- ⚠️ `Failed to fetch rate from instance`
- ⚠️ `Connection refused`

### Expected Behavior Summary

| Scenario | Expected Behavior |
|----------|-------------------|
| Single Producer | Consumer connects and fetches rates successfully |
| Multiple Producers | Requests distributed evenly across all instances |
| Dynamic Discovery | New producers automatically discovered without restart |
| Producer Failure | Consumer continues with remaining instances |
| Zookeeper Restart | All services reconnect and resume normal operation |

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Zookeeper Won't Start

**Symptoms:**
```
Error: Port 2181 is already in use
```

**Solutions:**
1. Check if Zookeeper is already running:
   ```bash
   docker ps | grep zookeeper
   ```

2. Stop existing Zookeeper:
   ```bash
   docker-compose down
   ```

3. Check for other processes using port 2181:
   ```bash
   # macOS/Linux
   lsof -i :2181
   
   # Windows
   netstat -ano | findstr :2181
   ```

4. Kill the process or change the port in `docker-compose.yml`

---

#### Issue 2: Producer Fails to Register

**Symptoms:**
```
ERROR c.e.c.registry.ServiceRegistry - Failed to register service
Connection refused: localhost:2181
```

**Solutions:**
1. Verify Zookeeper is running:
   ```bash
   docker-compose ps
   ```

2. Check Zookeeper health:
   ```bash
   docker exec zookeeper-server zkServer.sh status
   ```

3. Verify connection string in `application.properties`:
   ```properties
   zookeeper.connection-string=localhost:2181
   ```

4. Check firewall settings (ensure port 2181 is accessible)

---

#### Issue 3: Consumer Can't Discover Producers

**Symptoms:**
```
WARN c.e.r.discovery.ServiceDiscovery - No service instances available
```

**Solutions:**
1. Verify producers are running and registered:
   ```bash
   docker exec -it zookeeper-server zkCli.sh
   ls /services/currency-rate-provider/instances
   ```

2. Check producer logs for registration success

3. Verify consumer's Zookeeper connection string in `application.yml`

4. Ensure both producer and consumer use the same service path

---

#### Issue 4: Port Conflicts

**Symptoms:**
```
ERROR o.s.boot.web.embedded.tomcat.Tomcat - Port 9090 is already in use
```

**Solutions:**
1. Check what's using the port:
   ```bash
   # macOS/Linux
   lsof -i :9090
   
   # Windows
   netstat -ano | findstr :9090
   ```

2. Kill the process or use a different port:
   ```bash
   SERVER_PORT=9093 mvn spring-boot:run
   ```

3. Ensure each producer instance uses a unique port

---

#### Issue 5: Maven Build Failures

**Symptoms:**
```
[ERROR] Failed to execute goal on project
```

**Solutions:**
1. Clean Maven cache:
   ```bash
   mvn clean
   rm -rf ~/.m2/repository
   ```

2. Update dependencies:
   ```bash
   mvn dependency:resolve
   ```

3. Check Java version:
   ```bash
   java -version
   # Must be Java 17
   ```

4. Verify `pom.xml` is not corrupted

---

#### Issue 6: Load Balancing Not Working

**Symptoms:**
- All requests go to the same instance
- Round-robin pattern not observed

**Solutions:**
1. Verify multiple instances are registered:
   ```bash
   docker exec -it zookeeper-server zkCli.sh
   ls /services/currency-rate-provider/instances
   ```

2. Check consumer logs for instance discovery:
   ```
   INFO c.e.r.discovery.ServiceDiscovery - Discovered N service instance(s)
   ```

3. Ensure load balancer is properly initialized

4. Check for errors in `RoundRobinLoadBalancer` logs

---

### How to Clean Up Zookeeper Data

If you need to start fresh and remove all registered services:

**Option 1: Using zkCli**
```bash
docker exec -it zookeeper-server zkCli.sh
deleteall /services
quit
```

**Option 2: Restart Zookeeper with clean data**
```bash
docker-compose down -v
docker-compose up -d
```

The `-v` flag removes volumes, clearing all Zookeeper data.

---

### Debugging Tips

1. **Enable Debug Logging**
   
   Add to `application.properties` or `application.yml`:
   ```properties
   logging.level.com.example=DEBUG
   logging.level.org.apache.curator=DEBUG
   logging.level.org.apache.zookeeper=DEBUG
   ```

2. **Monitor Zookeeper in Real-Time**
   ```bash
   docker-compose logs -f zookeeper
   ```

3. **Check Network Connectivity**
   ```bash
   # Test Zookeeper connection
   telnet localhost 2181
   
   # Or using nc
   nc -zv localhost 2181
   ```

4. **Verify gRPC Connectivity**
   ```bash
   # Test if producer is listening
   telnet localhost 9090
   ```

5. **Use Zookeeper Four Letter Words**
   ```bash
   # Check server status
   echo stat | nc localhost 2181
   
   # List connections
   echo cons | nc localhost 2181
   
   # Environment info
   echo envi | nc localhost 2181
   ```

---

## Quick Reference Commands

### Start Everything
```bash
# Start Zookeeper
docker-compose up -d

# Start producers (in separate terminals)
cd currency-rate-provider
SERVER_PORT=9090 mvn spring-boot:run
SERVER_PORT=9091 mvn spring-boot:run
SERVER_PORT=9092 mvn spring-boot:run

# Start consumer (in another terminal)
cd rate-printer
mvn spring-boot:run
```

### Stop Everything
```bash
# Stop consumer and producers (Ctrl+C in each terminal)

# Stop Zookeeper
docker-compose down
```

### Clean Restart
```bash
# Stop and clean everything
docker-compose down -v
mvn clean

# Rebuild
mvn package

# Start fresh
docker-compose up -d
```

---

## Testing Checklist

Use this checklist to ensure comprehensive testing:

- [ ] Zookeeper starts successfully
- [ ] Single producer registers successfully
- [ ] Consumer discovers single producer
- [ ] Multiple producers register successfully
- [ ] Consumer discovers all producers
- [ ] Round-robin load balancing works correctly
- [ ] Consumer discovers producers added after startup
- [ ] Consumer handles producer failure gracefully
- [ ] Services reconnect after Zookeeper restart
- [ ] Zookeeper nodes are created correctly
- [ ] No errors in producer logs
- [ ] No errors in consumer logs
- [ ] Port conflicts are resolved
- [ ] All dependencies are installed

---

## Additional Resources

- [Apache Zookeeper Documentation](https://zookeeper.apache.org/doc/current/)
- [Apache Curator Framework](https://curator.apache.org/)
- [gRPC Java Documentation](https://grpc.io/docs/languages/java/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Architecture Design Document](zookeeper-integration-architecture.md)

---

## Support

If you encounter issues not covered in this guide:

1. Check the architecture design document for implementation details
2. Review the source code comments
3. Enable debug logging for more detailed information
4. Check Docker and Zookeeper logs for system-level issues

---

**Last Updated:** 2026-02-15
**Version:** 1.0