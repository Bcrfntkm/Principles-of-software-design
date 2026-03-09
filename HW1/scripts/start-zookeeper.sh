#!/bin/bash

# Script to start Zookeeper via Docker Compose
# Usage: ./start-zookeeper.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Starting Zookeeper..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Zookeeper is already running
if docker ps | grep -q zookeeper-server; then
    echo "Zookeeper is already running."
    echo "Container status:"
    docker ps | grep zookeeper-server
    exit 0
fi

# Navigate to project directory
cd "$PROJECT_DIR"

# Start Zookeeper using Docker Compose
echo "Starting Zookeeper container..."
docker-compose up -d zookeeper

# Wait for Zookeeper to be healthy
echo "Waiting for Zookeeper to be ready..."
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if docker exec zookeeper-server zkServer.sh status > /dev/null 2>&1; then
        echo "✓ Zookeeper is ready!"
        echo ""
        echo "Zookeeper is running on localhost:2181"
        echo ""
        echo "To check status: docker-compose ps"
        echo "To view logs: docker-compose logs -f zookeeper"
        echo "To stop: docker-compose stop zookeeper"
        exit 0
    fi
    
    ATTEMPT=$((ATTEMPT + 1))
    echo "Waiting... (attempt $ATTEMPT/$MAX_ATTEMPTS)"
    sleep 2
done

echo "Error: Zookeeper failed to start within expected time."
echo "Check logs with: docker-compose logs zookeeper"
exit 1