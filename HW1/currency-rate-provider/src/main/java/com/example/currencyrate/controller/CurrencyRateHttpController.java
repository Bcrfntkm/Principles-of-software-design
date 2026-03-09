package com.example.currencyrate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * HTTP gateway for currency rate service, used for Pact contract testing.
 */
@RestController
@RequestMapping("/api")
public class CurrencyRateHttpController {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateHttpController.class);
    
    private static final double BASE_RATE = 75.0;
    private static final double VARIATION = 2.0;
    private final Random random = new Random();

    @GetMapping("/rate")
    public ResponseEntity<Map<String, Object>> getRate() {
        try {
            double variation = (random.nextDouble() * 2 - 1) * VARIATION;
            double rate = Math.round((BASE_RATE + variation) * 100.0) / 100.0;
            
            logger.info("HTTP Gateway: Providing USD/RUB rate: {}", rate);
            
            Map<String, Object> result = new HashMap<>();
            result.put("rate", rate);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("HTTP Gateway: Error processing rate request", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve rate");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "currency-rate-provider");
        health.put("gateway", "http");
        return ResponseEntity.ok(health);
    }
}