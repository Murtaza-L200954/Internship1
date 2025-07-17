package org.example.demo1.common;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ResilientProductService {

    private static final Logger logger = Logger.getLogger(ResilientProductService.class.getName());

    private final SimulatedProductService simulatedService = new SimulatedProductService();

    private final Retry retry = Retry.of("productServiceRetry",
            RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofSeconds(1))
                    .build()
    );

    private final CircuitBreaker circuitBreaker = CircuitBreaker.of("productServiceCB",
            CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofSeconds(5))
                    .slidingWindowSize(5)
                    .build()
    );

    public ResilientProductService() {
        circuitBreaker.getEventPublisher()
                .onStateTransition(this::logStateChange);
    }

    public String getProductWithResilience() {
        Supplier<String> decorated = CircuitBreaker
                .decorateSupplier(circuitBreaker,
                        Retry.decorateSupplier(retry, simulatedService::getProductData)
                );

        return Try.ofSupplier(decorated)
                .onFailure(throwable -> logger.warning("Fallback triggered due to: " + throwable.getMessage()))
                .recover(throwable -> fallback())
                .get();
    }

    private String fallback() {
        return "Fallback: External product service not available.";
    }

    private void logStateChange(CircuitBreakerOnStateTransitionEvent event) {
        logger.info("CircuitBreaker '" + event.getCircuitBreakerName() + "' state changed from " +
                event.getStateTransition().getFromState() + " to " +
                event.getStateTransition().getToState());
    }
}
