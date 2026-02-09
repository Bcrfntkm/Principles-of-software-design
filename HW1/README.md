# Homework 1: gRPC Currency Rate Services

Two Java services communicating via gRPC. One provides USD/RUB exchange rates, the other fetches and prints them.

## Services

### 1. currency-rate-provider (Server)
- gRPC server on port 9090
- Returns random USD/RUB rates (70-80 range)
- See [currency-rate-provider/README.md](currency-rate-provider/README.md)

### 2. rate-printer (Client)
- gRPC client that connects to the server
- Fetches rates every 5 seconds
- Prints them to console
- See [rate-printer/README.md](rate-printer/README.md)

## Quick Start

**1. Start the server:**
```bash
cd currency-rate-provider
mvn clean install
mvn spring-boot:run
```

**2. In another terminal, start the client:**
```bash
cd rate-printer
mvn clean install
mvn spring-boot:run
```

**3. Watch the output:**
```
[2026-02-09 11:20:45] Current USD/RUB rate: 75.43
[2026-02-09 11:20:50] Current USD/RUB rate: 74.87
[2026-02-09 11:20:55] Current USD/RUB rate: 76.12
```

## Stopping the Services

### Simple Method (Recommended)
Press `Ctrl+C` in the terminal where the service is running. This will gracefully shut down the application.

### If Process is Stuck
If `Ctrl+C` doesn't work, you can find and kill the process manually:

**1. Find the process:**
```bash
# Show all Java processes
jps -l

# Or use ps for more details
ps aux | grep java
```

**2. Kill the process:**
```bash
# Using PID from the command above
kill <PID>

# If that doesn't work, force kill
kill -9 <PID>
```

**Example:**
```bash
$ jps -l
12345 com.example.currencyrateprovider.CurrencyRateProviderApplication
12346 com.example.rateprinter.RatePrinterApplication

$ kill 12345  # Stops currency-rate-provider
$ kill 12346  # Stops rate-printer
```

## Requirements

- Java 17+
- Maven 3.6+

## Architecture

```
┌─────────────────────────┐         gRPC          ┌─────────────────────────┐
│  currency-rate-provider │ ◄─────────────────── │     rate-printer        │
│   (Server)              │                       │   (Client)              │
│   Port: 9090            │   GetRate Request     │   Scheduled Task        │
│                         │ ─────────────────────►│   Every 5 seconds       │
│   Returns: USD/RUB rate │   GetRate Response    │                         │
└─────────────────────────┘                       └─────────────────────────┘
```

## Tech Stack

- Spring Boot 3.2.0
- gRPC 1.60.0
- Protocol Buffers 3.25.1
- Java 17

## Project Structure

```
HW1/
├── currency-rate-provider/     # gRPC server
│   ├── src/
│   ├── proto/
│   ├── resources/
│   ├── pom.xml
│   └── README.md
├── rate-printer/               # gRPC client
│   ├── service/
│   ├── proto/
│   ├── resources/
│   ├── pom.xml
│   └── README.md
└── README.md                   # This file