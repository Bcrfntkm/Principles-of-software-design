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
