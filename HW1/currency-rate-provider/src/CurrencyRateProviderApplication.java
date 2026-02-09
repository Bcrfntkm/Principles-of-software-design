package com.example.currencyrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Currency Rate Provider service.
 * This service provides USD/RUB exchange rates via gRPC.
 */
@SpringBootApplication
public class CurrencyRateProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyRateProviderApplication.class, args);
    }
}