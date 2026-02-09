package com.example.rateprinter.service;

import com.example.currencyrate.grpc.CurrencyRateServiceGrpc;
import com.example.currencyrate.grpc.GetRateRequest;
import com.example.currencyrate.grpc.GetRateResponse;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service that periodically queries the currency-rate-provider for USD/RUB exchange rates
 * and prints them to the console.
 */
@Service
public class RatePrinterService {

    private static final Logger logger = LoggerFactory.getLogger(RatePrinterService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GrpcClient("currency-rate-service")
    private CurrencyRateServiceGrpc.CurrencyRateServiceBlockingStub currencyRateStub;

    /**
     * Scheduled task that runs every 5 seconds to fetch and print the current exchange rate.
     * Initial delay of 2 seconds allows the application to fully start before the first execution.
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 2000)
    public void printCurrentRate() {
        try {
            GetRateRequest request = GetRateRequest.newBuilder().build();
            GetRateResponse response = currencyRateStub.getRate(request);
            
            String timestamp = LocalDateTime.now().format(formatter);
            String rateFormatted = String.format("%.2f", response.getRate());
            
            System.out.println(String.format("[%s] Current USD/RUB rate: %s", timestamp, rateFormatted));
            
        } catch (StatusRuntimeException e) {
            String timestamp = LocalDateTime.now().format(formatter);
            logger.error("[{}] ERROR: Unable to connect to currency-rate-provider service. " +
                    "Please ensure the server is running on localhost:9090", timestamp);
        } catch (Exception e) {
            String timestamp = LocalDateTime.now().format(formatter);
            logger.error("[{}] ERROR: Unexpected error occurred: {}", timestamp, e.getMessage());
        }
    }
}