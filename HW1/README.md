# Homework 1: Distributed gRPC Currency Rate Services with Zookeeper

A distributed system of Java services communicating via gRPC with Apache Zookeeper for service registry, discovery, and load balancing. Includes **Pact contract testing** for consumer-driven contract verification.

## Overview

- **Producers** (currency-rate-provider) register in Zookeeper and serve exchange rates via gRPC
- **Consumers** (rate-printer) dynamically discover producers through Zookeeper
- **Load balancing** distributes requests using round-robin algorithm
- **Automatic failover** ensures resilience when producers fail
- **Contract testing** with Pact ensures API compatibility between services

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker and Docker Compose
- (Optional) Pact Broker for contract management

## Quick Start

### 1. Start Infrastructure (Zookeeper + Pact Broker)

```bash
docker-compose up -d
```

This starts:
- **Zookeeper** on port 2181 (service discovery)
- **PostgreSQL** on port 5432 (Pact Broker database)
- **Pact Broker** on port 9292 (contract management)

Verify Pact Broker is running:
```bash
curl http://localhost:9292/diagnostic/status/heartbeat
```

Or visit: http://localhost:9292

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

### 4. Test Failover

Stop a producer (Ctrl+C) and watch the consumer automatically route to remaining instances.

## Stopping Services

```bash
# Stop all at once
./scripts/stop-all.sh

# Or individually
docker-compose down  # Zookeeper
Ctrl+C              # Producers/Consumer
```

## Contract Testing with Pact

### Overview

This project uses **Pact** for consumer-driven contract testing to ensure API compatibility between the rate-printer (consumer) and currency-rate-provider (provider).

### Architecture

```
rate-printer (Consumer)
    ↓ generates contract
Pact Broker
    ↓ fetches contract
currency-rate-provider (Provider)
    ↓ verifies implementation
```

### Running Contract Tests

#### Consumer Tests (rate-printer)

Generate and publish consumer contracts:

```bash
cd rate-printer
mvn clean test -Dtest=CurrencyRateConsumerTest
mvn pact:publish
```

This will:
1. Run consumer tests against a mock provider
2. Generate Pact contract files in `target/pacts/`
3. Publish contracts to Pact Broker at http://localhost:9292

#### Provider Tests (currency-rate-provider)

Verify provider against consumer contracts:

```bash
cd currency-rate-provider
mvn clean test -Dtest=CurrencyRateProviderTest
```

This will:
1. Fetch contracts from Pact Broker
2. Verify provider implementation satisfies all contracts
3. Report verification results back to Pact Broker

### HTTP Gateway for Contract Testing

Since Pact natively supports HTTP/JSON (not gRPC), the provider includes an HTTP gateway:

**Endpoint**: `GET http://localhost:8080/api/rate`

**Response**:
```json
{
  "rate": 75.42
}
```

This gateway mirrors the gRPC service functionality for contract testing purposes.

### Viewing Contracts

Access the Pact Broker UI:
```
http://localhost:9292
```

Features:
- View all consumer-provider contracts
- Check verification status
- Browse contract history
- Manage contract versions

### Contract Testing in CI/CD

GitHub Actions workflows automatically run contract tests:

- **Consumer workflow**: `.github/workflows/consumer-contract-tests.yml`
  - Runs on changes to `rate-printer/`
  - Publishes contracts to Pact Broker

- **Provider workflow**: `.github/workflows/provider-contract-tests.yml`
  - Runs on changes to `currency-rate-provider/`
  - Verifies against published contracts
  - Triggered by consumer contract changes

### Troubleshooting Contract Tests

**Issue**: Pact Broker not accessible
```bash
# Check if containers are running
docker-compose ps

# View Pact Broker logs
docker-compose logs pact-broker

# Restart services
docker-compose restart pact-broker postgres
```

**Issue**: Contract verification fails
```bash
# Check provider is running on correct port
curl http://localhost:8080/api/rate

# View detailed verification logs
cd currency-rate-provider
mvn test -Dtest=CurrencyRateProviderTest -X
```

**Issue**: Cannot publish contracts
```bash
# Verify Pact Broker URL
echo $PACT_BROKER_URL

# Test connectivity
curl http://localhost:9292/diagnostic/status/heartbeat

# Manually publish
cd rate-printer
mvn pact:publish -Dpact.broker.url=http://localhost:9292
```

## Project Structure

```
HW1/
├── currency-rate-provider/              # gRPC Producer
│   ├── src/
│   │   ├── com/example/currencyrate/
│   │   │   ├── controller/
│   │   │   │   └── CurrencyRateHttpController.java  # HTTP gateway for Pact
│   │   │   └── service/
│   │   │       └── CurrencyRateServiceImpl.java     # gRPC service
│   │   └── test/java/com/example/currencyrate/pact/
│   │       └── CurrencyRateProviderTest.java        # Provider verification
│   └── pom.xml                          # Pact provider dependencies
├── rate-printer/                        # gRPC Consumer
│   ├── src/test/java/com/example/rateprinter/pact/
│   │   └── CurrencyRateConsumerTest.java            # Consumer contract tests
│   └── pom.xml                          # Pact consumer dependencies
├── .github/workflows/
│   ├── consumer-contract-tests.yml      # Consumer CI/CD
│   └── provider-contract-tests.yml      # Provider CI/CD
├── scripts/                             # Utility scripts
├── docker-compose.yml                   # Infrastructure (Zookeeper + Pact Broker)
└── PACT_IMPLEMENTATION_PLAN.md          # Detailed implementation guide
```

## Tech Stack

- Spring Boot 3.2.0
- gRPC 1.60.0
- Apache Zookeeper 3.9.1
- Apache Curator 5.6.0
- **Pact 4.6.11** (Contract Testing)
- **Pact Broker 2.108.0** (Contract Management)
- PostgreSQL 15 (Pact Broker Database)
- Java 17

## Additional Resources

- [Pact Documentation](https://docs.pact.io/)
- [Pact Implementation Plan](PACT_IMPLEMENTATION_PLAN.md)
- [Consumer Contract Tests](rate-printer/src/test/java/com/example/rateprinter/pact/CurrencyRateConsumerTest.java)
- [Provider Verification Tests](currency-rate-provider/src/test/java/com/example/currencyrate/pact/CurrencyRateProviderTest.java)
