package com.vehiclerental.domain;

public class Vehicle {

    private final String id;
    private final String brand;
    private final String model;
    private final double dailyRate;
    private VehicleStatus status;

    public Vehicle(
            String id,
            String brand,
            String model,
            double dailyRate,
            VehicleStatus status) {

        this.id = id;
        this.brand = brand;
        this.model = model;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return VehicleStatus.AVAILABLE.equals(status);
    }

    public String getType() {
        return "STANDARD";
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", type='" + getType() + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", dailyRate=" + dailyRate +
                ", status=" + status +
                '}';
    }
}