package com.example.currencyrate;

import com.example.currencyrate.config.ZookeeperConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties(ZookeeperConfig.class)
public class CurrencyRateProviderApplication {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateProviderApplication.class);

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    public static void main(String[] args) {
        SpringApplication.run(CurrencyRateProviderApplication.class, args);
    }

    @PostConstruct
    public void logStartup() {
        logger.info("=== Starting Currency Rate Provider - Version: {} ===", appVersion);
    }
}