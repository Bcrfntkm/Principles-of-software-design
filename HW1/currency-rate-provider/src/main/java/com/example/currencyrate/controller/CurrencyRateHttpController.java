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
 * HTTP REST controller that provides an HTTP/JSON gateway to the gRPC currency rate service.
 * This controller is primarily used for Pact contract testing, allowing HTTP-based
 * contract verification while the actual service uses gRPC.
 * 
 * The controller replicates the same rate calculation logic as the gRPC service
 * to ensure consistency between HTTP and gRPC interfaces.
 */
@RestController
@RequestMapping("/api")
public class CurrencyRateHttpController {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateHttpController.class);
    
    private static final double BASE_RATE = 75.0;
    private static final double VARIATION = 2.0;
    private final Random random = new Random();

    /**
     * HTTP endpoint that returns the current USD/RUB exchange rate.
     * This endpoint mirrors the gRPC GetRate method functionality.
     * 
     * @return ResponseEntity containing a JSON object with the rate field
     * 
     * Example response:
     * {
     *   "rate": 75.42
     * }
     */
    @GetMapping("/rate")
    public ResponseEntity<Map<String, Object>> getRate() {
        try {
            // Calculate rate with random variation: BASE_RATE ± VARIATION
            double variation = (random.nextDouble() * 2 - 1) * VARIATION;
            double rate = BASE_RATE + variation;
            
            // Round to 2 decimal places for cleaner output
            rate = Math.round(rate * 100.0) / 100.0;
            
            logger.info("HTTP Gateway: Providing USD/RUB rate: {}", rate);
            
            // Build JSON response
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

    /**
     * Health check endpoint for the HTTP gateway.
     * Used to verify the service is running and accessible.
     * 
     * @return ResponseEntity with status information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "currency-rate-provider");
        health.put("gateway", "http");
        return ResponseEntity.ok(health);
    }
}