package com.example.rateprinter;

import com.example.rateprinter.config.ZookeeperConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Rate Printer service.
 * This service acts as a gRPC client that connects to the currency-rate-provider
 * and periodically queries and prints USD/RUB exchange rates.
 *
 * Uses Zookeeper for dynamic service discovery and round-robin load balancing
 * to distribute requests across multiple producer instances.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ZookeeperConfig.class)
public class RatePrinterApplication {

    private static final Logger logger = LoggerFactory.getLogger(RatePrinterApplication.class);

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    public static void main(String[] args) {
        SpringApplication.run(RatePrinterApplication.class, args);
    }

    @PostConstruct
    public void logStartup() {
        logger.info("=== Starting Rate Printer Client - Version: {} ===", appVersion);
    }
}