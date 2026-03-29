# Currency Rate Service

A gRPC-based microservices project demonstrating service discovery with Apache Zookeeper, load balancing, and observability with Spring Actuator + Micrometer + Prometheus + Grafana.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Docker Network                           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │  Zookeeper  │    │  Prometheus │    │       Grafana       │  │
│  │  port: 2181 │    │  port: 9091 │    │     port: 3000      │  │
│  └─────────────┘    └──────┬──────┘    └─────────────────────┘  │
└─────────────────────────────┼───────────────────────────────────┘
                              │ scrapes
        ┌─────────────────────┼──────────────────────┐
        ▼                                            ▼
┌───────────────────┐                    ┌───────────────────┐
│ currency-rate-    │  gRPC (port 9090)  │   rate-printer    │
│ provider          │◄───────────────────│   (client)        │
│ (server)          │                    │                   │
│ Actuator: 8081    │                    │ Actuator: 8082    │
└───────────────────┘                    └───────────────────┘
        │                                        │
        └──────────── Zookeeper ─────────────────┘
                   (service discovery)
```

## Services

| Service | Description | Ports |
|---------|-------------|-------|
| `currency-rate-provider` | gRPC server providing USD/RUB exchange rates | gRPC: 9090, Actuator: 8081 |
| `rate-printer` | gRPC client fetching rates every 5s via Zookeeper discovery | Actuator: 8082 |
| `zookeeper` | Service registry for dynamic service discovery | 2181 |
| `prometheus` | Metrics collection | 9091 |
| `grafana` | Metrics visualization | 3000 |

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose

## Quick Start

### Step 1: Start Infrastructure (Zookeeper + Monitoring)

```bash
cd BIT/Principles-of-software-design/HW1
docker-compose up -d
```

This starts: Zookeeper, Prometheus, Grafana, PostgreSQL (for Pact Broker), and Pact Broker.

### Step 2: Start the Provider (gRPC Server)

```bash
cd BIT/Principles-of-software-design/HW1/currency-rate-provider
mvn spring-boot:run
```

Or with a custom port:
```bash
SERVER_PORT=9091 mvn spring-boot:run
```

The provider:
- Starts gRPC server on port 9090 (configurable via `SERVER_PORT`)
- Registers itself in Zookeeper at `/services/currency-rate-provider`
- Exposes Spring Actuator on port **8081**

### Step 3: Start the Consumer (gRPC Client)

In a new terminal:
```bash
cd BIT/Principles-of-software-design/HW1/rate-printer
mvn spring-boot:run
```

The consumer:
- Discovers providers via Zookeeper
- Fetches USD/RUB rates every 5 seconds using round-robin load balancing
- Exposes Spring Actuator on port **8082**

## Monitoring

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9091 | - |
| Provider Actuator | http://localhost:8081/actuator | - |
| Consumer Actuator | http://localhost:8082/actuator | - |

### Grafana Dashboard

Navigate to **Dashboards → Spring Boot → JVM Micrometer - Currency Rate Services**

The dashboard shows:
- JVM Heap & Non-Heap Memory usage
- Thread counts (live + daemon)
- CPU usage (process + system)
- GC pause duration
- **gRPC Request Rate** (requests/second by method and client)
- **gRPC Request Duration** (p50, p95, p99 percentiles)
- **gRPC Error Rate** (500-level errors)

Use the **Application** dropdown to filter by service (`currency-rate-provider` or `rate-printer`).

### Prometheus Targets

Check that both services are being scraped: http://localhost:9091/targets

Expected status:
- `currency-rate-provider` → **UP**
- `rate-printer` → **UP**

### Verify Metrics Manually

```bash
# Provider metrics
curl http://localhost:8081/actuator/prometheus | grep grpc_server

# Consumer metrics  
curl http://localhost:8082/actuator/prometheus | grep jvm_memory_used
```

## Metrics Reference

### JVM Metrics (auto-collected by Micrometer)

| Metric | Description |
|--------|-------------|
| `jvm_memory_used_bytes` | Memory usage by area (heap/nonheap) |
| `jvm_threads_live_threads` | Active thread count |
| `process_cpu_usage` | Process CPU usage (0-1) |
| `jvm_gc_pause_seconds` | GC pause duration |

### Custom Application Metrics (currency-rate-provider)

| Metric | Type | Tags | Description |
|--------|------|------|-------------|
| `grpc_server_requests_total` | Counter | `method`, `client` | Total gRPC requests |
| `grpc_server_errors_total` | Counter | `method`, `error_type` | Total gRPC errors (500-level) |
| `grpc_server_request_duration_seconds` | Timer | `method`, `status` | Request processing time |

## Logs

### What Gets Logged

**On startup (both services):**
```
=== Starting Currency Rate Provider - Version: 1.0.0 ===
=== Starting Rate Printer Client - Version: 1.0.0 ===
```

**Server (currency-rate-provider):**
```
[SERVER] Received gRPC request: GetRate()
[SERVER] Sending response: USD/RUB rate = 75.42
[SERVER] Received HTTP request: GET /api/rate
```

**Client (rate-printer):**
```
[CLIENT] Sending gRPC request: GetRate() to localhost:9090
[CLIENT] Received response: USD/RUB rate = 75.42
```

## HTTP Endpoints (currency-rate-provider)

The provider also exposes HTTP endpoints used for Pact contract testing:

| Endpoint | Description |
|----------|-------------|
| `GET /api/rate` | Returns current USD/RUB rate as JSON |
| `GET /api/health` | Health check |

These are used by the Pact consumer/provider contract tests.

## Contract Testing (Pact)

The project includes Pact contract tests:

```bash
# Start Pact Broker
docker-compose up -d pact-broker

# Run consumer tests (generates pact file)
cd rate-printer && mvn test

# Run provider verification
cd currency-rate-provider && mvn test
```

Pact Broker UI: http://localhost:9292

## Running Multiple Provider Instances

The consumer supports round-robin load balancing across multiple provider instances:

```bash
# Terminal 1
cd currency-rate-provider && SERVER_PORT=9090 mvn spring-boot:run

# Terminal 2
cd currency-rate-provider && SERVER_PORT=9091 mvn spring-boot:run

# Terminal 3 (consumer will alternate between both)
cd rate-printer && mvn spring-boot:run
```

## Stopping Everything

```bash
# Stop Spring Boot apps
pkill -f "spring-boot:run"

# Stop Docker services
cd BIT/Principles-of-software-design/HW1
docker-compose down

# Remove volumes (clears all data)
docker-compose down -v
```
