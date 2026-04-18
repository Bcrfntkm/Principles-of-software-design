package com.example.rateprinter.service;

import com.example.currencyrate.grpc.CurrencyRateServiceGrpc;
import com.example.currencyrate.grpc.GetRateRequest;
import com.example.currencyrate.grpc.GetRateResponse;
import com.example.rateprinter.loadbalancer.RoundRobinLoadBalancer;
import com.example.rateprinter.model.ServiceInstance;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Periodically queries currency-rate-provider for USD/RUB rates using service discovery and load balancing.
 */
@Service
public class RatePrinterService {

    private static final Logger logger = LoggerFactory.getLogger(RatePrinterService.class);
    private volatile boolean running = true;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final RoundRobinLoadBalancer loadBalancer;

    public RatePrinterService(RoundRobinLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * Scheduled task that runs every 5 seconds to fetch and print the current exchange rate.
     * Initial delay of 2 seconds allows the application to fully start before the first execution.
     * 
     * Uses dynamic service discovery to get the next available producer instance and creates
     * a gRPC channel on-demand for each request.
     */
    @Scheduled(fixedDelayString = "${rate-printer.interval-ms:5000}", initialDelay = 2000)
    public void printCurrentRate() {
        if (!running) return;
        ManagedChannel channel = null;
        ServiceInstance instance = null;
        
        try {
            // Get next instance from load balancer
            instance = loadBalancer.getNextInstance();
            
            logger.info("[CLIENT] Sending gRPC request: GetRate() to {}", instance.getAddress());
            
            channel = ManagedChannelBuilder
                .forTarget(instance.getAddress())
                .usePlaintext()
                .build();
            
            CurrencyRateServiceGrpc.CurrencyRateServiceBlockingStub stub =
                CurrencyRateServiceGrpc.newBlockingStub(channel);
            
            GetRateRequest request = GetRateRequest.newBuilder().build();
            GetRateResponse response = stub.getRate(request);
            
            logger.info("[CLIENT] Received response: USD/RUB rate = {}", response.getRate());
            
            String timestamp = LocalDateTime.now().format(formatter);
            String rateFormatted = String.format("%.2f", response.getRate());
            
            System.out.println(String.format("[%s] Current USD/RUB rate: %s (from instance: %s)",
                timestamp, rateFormatted, instance.getInstanceId()));
            
        } catch (RoundRobinLoadBalancer.NoAvailableInstancesException e) {
            String timestamp = LocalDateTime.now().format(formatter);
            logger.error("[CLIENT] Error fetching rate: No available instances. " +
                    "Please ensure at least one producer is running and registered in Zookeeper.");
            
        } catch (StatusRuntimeException e) {
            String timestamp = LocalDateTime.now().format(formatter);
            String address = instance != null ? instance.getAddress() : "unknown";
            logger.error("[CLIENT] Error fetching rate from {}: {}", address, e.getStatus());
            
        } catch (Exception e) {
            String timestamp = LocalDateTime.now().format(formatter);
            String address = instance != null ? instance.getAddress() : "unknown";
            logger.error("[CLIENT] Error fetching rate from {}: {}", address, e.getMessage(), e);
            
        } finally {
            // Always close the channel to free resources
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.warn("Channel shutdown interrupted", e);
                    channel.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        this.running = false;
        logger.info("Shutting down RatePrinterService...");
    }
}