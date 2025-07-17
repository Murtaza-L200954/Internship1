package org.example.demo1.common;

import java.util.Random;

public class SimulatedProductService {

    private final Random random = new Random();

    public String getProductData() {
        if (random.nextInt(10) < 7) { // 70% chance to fail
            throw new RuntimeException("Temporary product service failure");
        }
        return "Product Data from external system";
    }
}
