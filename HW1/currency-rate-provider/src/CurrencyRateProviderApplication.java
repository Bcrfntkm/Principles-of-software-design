package com.example.currencyrate;

import com.example.currencyrate.config.ZookeeperConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main Spring Boot application class for Currency Rate Provider service.
 * This service provides USD/RUB exchange rates via gRPC and automatically
 * registers itself in Zookeeper for service discovery.
 */
@SpringBootApplication
@EnableConfigurationProperties(ZookeeperConfig.class)
public class CurrencyRateProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyRateProviderApplication.class, args);
    }
}