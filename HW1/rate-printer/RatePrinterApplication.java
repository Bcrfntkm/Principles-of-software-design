package com.example.rateprinter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Rate Printer service.
 * This service acts as a gRPC client that connects to the currency-rate-provider
 * and periodically queries and prints USD/RUB exchange rates.
 */
@SpringBootApplication
@EnableScheduling
public class RatePrinterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RatePrinterApplication.class, args);
    }
}