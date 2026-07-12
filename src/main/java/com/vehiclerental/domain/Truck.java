package com.vehiclerental.domain;

public class Truck extends Vehicle {
    public Truck(String id, String brand, String model, double dailyRate, VehicleStatus status) {
        super(id, brand, model, dailyRate, status);
    }

    @Override
    public String getType() { return "TRUCK"; }
}
