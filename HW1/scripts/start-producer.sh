#!/bin/bash

# Script to start currency-rate-provider with configurable port
# Usage: ./start-producer.sh [port]
# Default port: 9090

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
PRODUCER_DIR="$PROJECT_DIR/currency-rate-provider"

# Get port from argument or use default
PORT=${1:-9090}

echo "Starting Currency Rate Provider on port $PORT..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven and try again."
    exit 1
fi

# Check if Zookeeper is running
if ! docker ps | grep -q zookeeper-server; then
    echo "Warning: Zookeeper is not running."
    echo "Starting Zookeeper first..."
    "$SCRIPT_DIR/start-zookeeper.sh"
    echo ""
fi

# Navigate to producer directory
cd "$PRODUCER_DIR"

# Check if project is built
if [ ! -f "target/currency-rate-provider-1.0-SNAPSHOT.jar" ]; then
    echo "Building producer project..."
    mvn clean package -DskipTests
    echo ""
fi

# Start the producer
echo "Starting producer on port $PORT..."
echo "Press Ctrl+C to stop"
echo ""

export SERVER_PORT=$PORT
mvn spring-boot:run