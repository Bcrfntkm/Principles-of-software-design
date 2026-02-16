#!/bin/bash

# Script to stop all services including Zookeeper
# Usage: ./stop-all.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Stopping all services..."

# Navigate to project directory
cd "$PROJECT_DIR"

# Stop Zookeeper
if docker ps | grep -q zookeeper-server; then
    echo "Stopping Zookeeper..."
    docker-compose stop zookeeper
    echo "✓ Zookeeper stopped"
else
    echo "Zookeeper is not running"
fi

# Note: Producer and consumer processes need to be stopped manually with Ctrl+C
# or by killing their processes
echo ""
echo "Note: If you have producer or consumer processes running,"
echo "please stop them manually with Ctrl+C in their respective terminals."
echo ""

# Optional: Find and display Java processes that might be producers/consumers
if command -v jps &> /dev/null; then
    echo "Active Java processes:"
    jps -l | grep -E "(CurrencyRateProviderApplication|RatePrinterApplication)" || echo "No producer/consumer processes found"
fi

echo ""
echo "To completely remove Zookeeper container and volumes:"
echo "  docker-compose down -v"