package com.fabish.hotel.model;

public enum RoomType {
    SINGLE("Single Room", 1, 100.0),
    DOUBLE("Double Room", 2, 150.0),
    TWIN("Twin Room", 2, 150.0),
    DELUXE("Deluxe Room", 2, 200.0),
    SUITE("Suite", 4, 300.0),
    EXECUTIVE_SUITE("Executive Suite", 4, 400.0),
    PRESIDENTIAL_SUITE("Presidential Suite", 6, 800.0);

    private final String displayName;
    private final int capacity;
    private final double basePrice;

    RoomType(String displayName, int capacity, double basePrice) {
        this.displayName = displayName;
        this.capacity = capacity;
        this.basePrice = basePrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getBasePrice() {
        return basePrice;
    }
} 