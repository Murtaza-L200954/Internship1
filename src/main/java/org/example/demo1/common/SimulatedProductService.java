package org.example.demo1.common;

import java.util.Random;

public class SimulatedProductService {
    private final Random random = new Random();

    public String getProductData() {
        // Simulate latency
        try {
            Thread.sleep(500 + random.nextInt(1000)); // 500–1500ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate failure (70% chance)
        if (random.nextInt(10) < 7) {
            throw new RuntimeException("Simulated API failure");
        }

        return "Product Data from external system";
    }
}

