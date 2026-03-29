package com.example.rateprinter.pact;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pact consumer test defining the contract between rate-printer and currency-rate-provider.
 */
@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CurrencyRateConsumerTest {

    @Pact(consumer = "rate-printer", provider = "currency-rate-provider")
    public V4Pact createPactForGetRate(PactDslWithProvider builder) {
        return builder
            .given("provider is running")
            .uponReceiving("a request for current USD/RUB rate")
                .path("/api/rate")
                .method("GET")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(
                    new au.com.dius.pact.consumer.dsl.PactDslJsonBody()
                        .decimalType("rate", 75.0)
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "rate-printer", provider = "currency-rate-provider")
    public V4Pact createPactForHighRate(PactDslWithProvider builder) {
        return builder
            .given("provider with high rate")
            .uponReceiving("a request for high USD/RUB rate")
                .path("/api/rate")
                .method("GET")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(
                    new au.com.dius.pact.consumer.dsl.PactDslJsonBody()
                        .decimalType("rate", 76.5)
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "rate-printer", provider = "currency-rate-provider")
    public V4Pact createPactForLowRate(PactDslWithProvider builder) {
        return builder
            .given("provider with low rate")
            .uponReceiving("a request for low USD/RUB rate")
                .path("/api/rate")
                .method("GET")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(
                    new au.com.dius.pact.consumer.dsl.PactDslJsonBody()
                        .decimalType("rate", 73.5)
                )
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createPactForGetRate", port = "8080")
    public void testGetRate() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/rate";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("rate"));
        
        Double rate = ((Number) response.getBody().get("rate")).doubleValue();
        assertNotNull(rate);
        assertTrue(rate >= 73.0 && rate <= 77.0, 
            "Rate should be between 73.0 and 77.0, but was: " + rate);
    }

    @Test
    @PactTestFor(pactMethod = "createPactForHighRate", port = "8081")
    public void testGetHighRate() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/api/rate";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("rate"));
        
        Double rate = ((Number) response.getBody().get("rate")).doubleValue();
        assertNotNull(rate);
        assertTrue(rate >= 76.0 && rate <= 77.0, 
            "High rate should be between 76.0 and 77.0, but was: " + rate);
    }

    @Test
    @PactTestFor(pactMethod = "createPactForLowRate", port = "8082")
    public void testGetLowRate() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8082/api/rate";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("rate"));
        
        Double rate = ((Number) response.getBody().get("rate")).doubleValue();
        assertNotNull(rate);
        assertTrue(rate >= 73.0 && rate <= 74.0, 
            "Low rate should be between 73.0 and 74.0, but was: " + rate);
    }

    @Test
    @org.junit.jupiter.api.Disabled("Not a contract test - tests error handling only")
    public void testConsumerHandlesProviderUnavailable() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:9999/api/rate";

        assertThrows(Exception.class, () -> {
            restTemplate.getForEntity(url, Map.class);
        }, "Consumer should throw exception when provider is unavailable");
    }
}