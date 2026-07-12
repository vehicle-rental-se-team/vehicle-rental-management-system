package com.vehiclerental.domain;

public class Van extends Vehicle {
    public Van(String id, String brand, String model, double dailyRate, VehicleStatus status) {
        super(id, brand, model, dailyRate, status);
    }

    @Override
    public String getType() { return "VAN"; }
}
