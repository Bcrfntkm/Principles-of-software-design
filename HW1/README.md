# Homework 1: Distributed gRPC Currency Rate Services with Zookeeper

A distributed system of Java services communicating via gRPC with Apache Zookeeper for service registry, discovery, and load balancing.

## Overview

- **Producers** (currency-rate-provider) register in Zookeeper and serve exchange rates via gRPC
- **Consumers** (rate-printer) dynamically discover producers through Zookeeper
- **Load balancing** distributes requests using round-robin algorithm
- **Automatic failover** ensures resilience when producers fail

## Features

✅ Automatic Service Registration  
✅ Dynamic Service Discovery  
✅ Round-Robin Load Balancing  
✅ Automatic Failover  
✅ Connection Resilience  

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker and Docker Compose

## Quick Start

### 1. Start Zookeeper

```bash
docker-compose up -d
```

### 2. Start Producer(s)

```bash
cd currency-rate-provider
mvn clean install
mvn spring-boot:run

# Optional: Start additional instances on different ports
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9091 --grpc.server.port=9091"
```

### 3. Start Consumer

```bash
cd rate-printer
mvn clean install
mvn spring-boot:run
```

### 4. Watch Output

```
[2026-02-15 16:07:23] Current USD/RUB rate: 75.43 (from instance: instance-abc123)
[2026-02-15 16:07:28] Current USD/RUB rate: 74.87 (from instance: instance-def456)
```

### 5. Test Failover

Stop a producer (Ctrl+C) and watch the consumer automatically route to remaining instances.

## Stopping Services

```bash
# Stop all at once
./scripts/stop-all.sh

# Or individually
docker-compose down  # Zookeeper
Ctrl+C              # Producers/Consumer
```

## Project Structure

```
HW1/
├── currency-rate-provider/     # gRPC Producer
├── rate-printer/               # gRPC Consumer
├── scripts/                    # Utility scripts
├── docker-compose.yml          # Zookeeper setup
├── TESTING.md                  # Testing guide
└── zookeeper-integration-architecture.md  # Architecture details
```

## Tech Stack

- Spring Boot 3.2.0
- gRPC 1.60.0
- Apache Zookeeper 3.9.1
- Apache Curator 5.5.0
- Java 17

## Documentation

- **[zookeeper-integration-architecture.md](zookeeper-integration-architecture.md)** - Detailed architecture and design
- **[TESTING.md](TESTING.md)** - Comprehensive testing scenarios

## Troubleshooting

### Zookeeper Issues
```bash
docker ps | grep zookeeper
docker logs zookeeper
```

### Port Conflicts
```bash
lsof -i :9090
kill -9 <PID>
```

See [TESTING.md](TESTING.md#troubleshooting) for more details.

## License

Educational project for BIT - Architecture Design course.