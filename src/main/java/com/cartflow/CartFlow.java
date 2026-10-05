package com.cartflow;

public class CartFlow {

    public static String getWelcomeMessage() {
        return "Welcome to CartFlow";
    }

    public static String getHealthStatus() {
        return "CartFlow is healthy";
    }

    public static void main(String[] args) {
        System.out.println(getWelcomeMessage());
        System.out.println(getHealthStatus());
    }
}
