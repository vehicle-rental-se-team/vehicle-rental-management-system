package com.vehiclerental.domain;

public class Motorcycle extends Vehicle {
    public Motorcycle(String id, String brand, String model, double dailyRate, VehicleStatus status) {
        super(id, brand, model, dailyRate, status);
    }

    @Override
    public String getType() { return "MOTORCYCLE"; }
}
