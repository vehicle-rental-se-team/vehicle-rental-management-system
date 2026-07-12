package com.vehiclerental.domain;

public class ElectricVehicle extends Vehicle {
    private final int batteryLevel;

    public ElectricVehicle(String id, String brand, String model, double dailyRate,
                           VehicleStatus status, int batteryLevel) {
        super(id, brand, model, dailyRate, status);
        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new IllegalArgumentException("Battery level must be between 0 and 100.");
        }
        this.batteryLevel = batteryLevel;
    }

    public int getBatteryLevel() { return batteryLevel; }

    @Override
    public String getType() { return "ELECTRIC"; }
}
