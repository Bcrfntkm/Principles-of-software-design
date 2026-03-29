package com.example.currencyrate.service;

import com.example.currencyrate.grpc.CurrencyRateServiceGrpc;
import com.example.currencyrate.grpc.GetRateRequest;
import com.example.currencyrate.grpc.GetRateResponse;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
    private final MeterRegistry meterRegistry;

    /**
     * Constructor with MeterRegistry for custom metrics.
     *
     * @param meterRegistry the Micrometer meter registry
     */
    public CurrencyRateServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Implements the GetRate RPC method.
     * Returns the current USD/RUB exchange rate with randomization.
     *
     * @param request the GetRateRequest (empty)
     * @param responseObserver the response observer to send the rate back
     */
    @Override
    public void getRate(GetRateRequest request, StreamObserver<GetRateResponse> responseObserver) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            logger.info("[SERVER] Received gRPC request: GetRate()");
            
            Counter.builder("grpc.server.requests.total")
                .tag("method", "GetRate")
                .tag("client", "unknown")
                .description("Total number of gRPC requests")
                .register(meterRegistry)
                .increment();
            
            double variation = (random.nextDouble() * 2 - 1) * VARIATION; // Random value between -2.0 and +2.0
            double rate = BASE_RATE + variation;
            
            rate = Math.round(rate * 100.0) / 100.0;
            
            logger.info("[SERVER] Sending response: USD/RUB rate = {}", rate);
            
            GetRateResponse response = GetRateResponse.newBuilder()
                    .setRate(rate)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            sample.stop(Timer.builder("grpc.server.request.duration")
                .tag("method", "GetRate")
                .tag("status", "success")
                .description("gRPC request processing duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));
            
        } catch (Exception e) {
            Counter.builder("grpc.server.errors.total")
                .tag("method", "GetRate")
                .tag("error_type", "INTERNAL")
                .description("Total number of gRPC server errors")
                .register(meterRegistry)
                .increment();
            
            sample.stop(Timer.builder("grpc.server.request.duration")
                .tag("method", "GetRate")
                .tag("status", "error")
                .description("gRPC request processing duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry));
            
            logger.error("[SERVER] Error processing GetRate request: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
}