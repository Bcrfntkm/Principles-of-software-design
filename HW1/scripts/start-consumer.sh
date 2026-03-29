#!/bin/bash

# Script to start rate-printer (consumer)
# Usage: ./start-consumer.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CONSUMER_DIR="$PROJECT_DIR/rate-printer"

echo "Starting Rate Printer (Consumer)..."

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

# Navigate to consumer directory
cd "$CONSUMER_DIR"

# Check if project is built
if [ ! -f "target/rate-printer-1.0-SNAPSHOT.jar" ]; then
    echo "Building consumer project..."
    mvn clean package -DskipTests
    echo ""
fi

# Start the consumer
echo "Starting consumer..."
echo "Press Ctrl+C to stop"
echo ""

mvn spring-boot:run