# Homework 1: Distributed gRPC Currency Rate Services

Distributed Java services communicating via gRPC with Apache Zookeeper for service discovery and load balancing. Includes Pact contract testing.

## Overview

- **Producers** (currency-rate-provider) register in Zookeeper and serve exchange rates via gRPC
- **Consumers** (rate-printer) discover producers through Zookeeper
- **Load balancing** using round-robin algorithm
- **Automatic failover** when producers fail
- **Contract testing** with Pact for API compatibility

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker and Docker Compose

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

Starts Zookeeper (port 2181) and Pact Broker (port 9292).

### 2. Start Producer

```bash
cd currency-rate-provider
mvn clean install
mvn spring-boot:run

# Additional instances on different ports:
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9091 --grpc.server.port=9091"
```

### 3. Start Consumer

```bash
cd rate-printer
mvn clean install
mvn spring-boot:run
```

### 4. Stop Services

```bash
./scripts/stop-all.sh
# or
docker-compose down
```

## Contract Testing

### Consumer Tests

Generate and publish contracts:

```bash
cd rate-printer
mvn clean test -Dtest=CurrencyRateConsumerTest
mvn pact:publish
```

### Provider Tests

Verify provider against contracts:

```bash
cd currency-rate-provider
mvn clean test -Dtest=CurrencyRateProviderTest
```

### HTTP Gateway

Provider includes HTTP endpoint for Pact testing:

**Endpoint**: `GET http://localhost:8080/api/rate`

**Response**:
```json
{
  "rate": 75.42
}
```

### Pact Broker

Access at http://localhost:9292 to view contracts and verification status.

### CI/CD

GitHub Actions workflows in `.github/workflows/` run contract tests automatically.

## Project Structure

```
HW1/
├── currency-rate-provider/
│   ├── src/main/java/com/example/currencyrate/
│   │   ├── controller/CurrencyRateHttpController.java
│   │   └── service/CurrencyRateServiceImpl.java
│   └── src/test/java/com/example/currencyrate/pact/
│       └── CurrencyRateProviderTest.java
├── rate-printer/
│   └── src/test/java/com/example/rateprinter/pact/
│       └── CurrencyRateConsumerTest.java
├── .github/workflows/
├── scripts/
└── docker-compose.yml
```

## Tech Stack

- Spring Boot 3.2.0
- gRPC 1.60.0
- Apache Zookeeper 3.9.1
- Apache Curator 5.6.0
- Pact 4.6.11
- Java 17

## Monitoring Setup

### Overview
The project includes a complete monitoring stack with Prometheus and Grafana for observing JVM metrics and custom application metrics.

### Services
| Service | Port | Description |
|---------|------|-------------|
| currency-rate-provider (Actuator) | 8081 | Spring Boot Actuator metrics |
| rate-printer (Actuator) | 8082 | Spring Boot Actuator metrics |
| Prometheus | 9091 | Metrics collection |
| Grafana | 3000 | Metrics visualization |

### Starting the Monitoring Stack

1. Start the Spring Boot services first:
```bash
# Terminal 1 - Start provider
./scripts/start-producer.sh

# Terminal 2 - Start consumer
./scripts/start-consumer.sh
```

2. Start the full infrastructure (including monitoring):
```bash
docker-compose up -d
```

### Accessing Monitoring

- **Prometheus**: http://localhost:9091
  - Check targets: http://localhost:9091/targets
- **Grafana**: http://localhost:3000 (admin/admin)
  - JVM Dashboard: Navigate to Dashboards → Spring Boot → JVM Micrometer

### Metrics Available

#### JVM Metrics (auto-collected by Micrometer)
- `jvm_memory_used_bytes` - JVM memory usage by area (heap/nonheap)
- `jvm_threads_live_threads` - Active thread count
- `process_cpu_usage` - CPU usage
- `jvm_gc_pause_seconds` - GC pause duration

#### Custom Application Metrics
- `grpc_server_requests_total` - Total gRPC requests (tagged by method, client)
- `grpc_server_errors_total` - Total gRPC errors (tagged by method, error_type)
- `grpc_server_request_duration_seconds` - Request duration with p50/p95/p99 percentiles

### Verify Metrics Collection

```bash
# Check provider metrics
curl http://localhost:8081/actuator/prometheus | grep grpc

# Check consumer metrics
curl http://localhost:8082/actuator/prometheus | grep jvm_memory
```
