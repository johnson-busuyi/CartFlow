package com.cartflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartFlowTest {

    @Test
    void testWelcomeMessage() {
        assertEquals(
            "Welcome to CartFlow",
            CartFlow.getWelcomeMessage()
        );
    }

    @Test
    void testHealthStatus() {
        assertEquals(
            "CartFlow is healthy",
            CartFlow.getHealthStatus()
        );
    }
}
