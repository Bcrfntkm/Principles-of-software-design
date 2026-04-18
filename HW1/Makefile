.PHONY: build release run-provider run-consumer stop clean

# Stage 1: Build — compile and package JARs
build:
	cd currency-rate-provider && mvn clean package -DskipTests
	cd rate-printer && mvn clean package -DskipTests

# Stage 2: Release — build Docker images (immutable artifacts)
release: build
	docker build -t currency-rate-provider:latest currency-rate-provider/
	docker build -t rate-printer:latest rate-printer/

# Stage 3: Run — start services
run-provider:
	bash scripts/start-producer.sh

run-consumer:
	bash scripts/start-consumer.sh

stop:
	bash scripts/stop-all.sh

clean:
	cd currency-rate-provider && mvn clean
	cd rate-printer && mvn clean
