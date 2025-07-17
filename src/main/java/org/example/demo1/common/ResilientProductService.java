package org.example.demo1.common;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.function.Supplier;

public class ResilientProductService {

    private final SimulatedProductService simulatedService = new SimulatedProductService();

    // Retry config: max 3 attempts with 1-second wait between
    private final RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .build();

    private final Retry retry = Retry.of("productServiceRetry", config);

    public String getProductWithRetry() {
        Supplier<String> supplier = Retry.decorateSupplier(retry, simulatedService::getProductData);

        return Try.ofSupplier(supplier)
                .recover(throwable -> "Fallback: Could not fetch product data")
                .get();
    }
}
