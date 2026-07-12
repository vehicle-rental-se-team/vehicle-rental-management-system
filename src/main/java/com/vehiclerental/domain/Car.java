package com.vehiclerental.domain;

public class Car extends Vehicle {
    public Car(String id, String brand, String model, double dailyRate, VehicleStatus status) {
        super(id, brand, model, dailyRate, status);
    }

    @Override
    public String getType() { return "CAR"; }
}
