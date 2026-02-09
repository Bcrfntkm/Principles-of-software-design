package com.example.currencyrate.service;

import com.example.currencyrate.grpc.CurrencyRateServiceGrpc;
import com.example.currencyrate.grpc.GetRateRequest;
import com.example.currencyrate.grpc.GetRateResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * gRPC service implementation for providing USD/RUB currency rates.
 * Returns a base rate of 75.0 with random variation of ±2.0 on each request.
 */
@GrpcService
public class CurrencyRateServiceImpl extends CurrencyRateServiceGrpc.CurrencyRateServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateServiceImpl.class);
    
    private static final double BASE_RATE = 75.0;
    private static final double VARIATION = 2.0;
    private final Random random = new Random();

    /**
     * Implements the GetRate RPC method.
     * Returns the current USD/RUB exchange rate with randomization.
     *
     * @param request the GetRateRequest (empty)
     * @param responseObserver the response observer to send the rate back
     */
    @Override
    public void getRate(GetRateRequest request, StreamObserver<GetRateResponse> responseObserver) {
        try {
            // Calculate rate with random variation: BASE_RATE ± VARIATION
            double variation = (random.nextDouble() * 2 - 1) * VARIATION; // Random value between -2.0 and +2.0
            double rate = BASE_RATE + variation;
            
            // Round to 2 decimal places for cleaner output
            rate = Math.round(rate * 100.0) / 100.0;
            
            logger.info("Providing USD/RUB rate: {}", rate);
            
            // Build and send response
            GetRateResponse response = GetRateResponse.newBuilder()
                    .setRate(rate)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            logger.error("Error processing GetRate request", e);
            responseObserver.onError(e);
        }
    }
}