package com.example.currencyrate.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.example.currencyrate.controller.CurrencyRateHttpController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pact provider verification test.
 * Verifies that the provider satisfies consumer contracts from the Pact Broker.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(CurrencyRateHttpController.class)
@Provider("currency-rate-provider")
@PactBroker(url = "http://localhost:9292")
public class CurrencyRateProviderTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup(PactVerificationContext context) {
        context.setTarget(new MockMvcTestTarget(mockMvc));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("provider is running")
    void providerIsRunning() {
        // Default state - controller returns rate between 73.0 and 77.0
    }

    @State("provider with high rate")
    void providerWithHighRate() {
        // Random generation naturally covers high rate range (76.0 - 77.0)
    }

    @State("provider with low rate")
    void providerWithLowRate() {
        // Random generation naturally covers low rate range (73.0 - 74.0)
    }
}