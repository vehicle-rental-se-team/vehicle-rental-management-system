package com.vehiclerental.domain;

public class FuelRecord {

    private final String vehicleId;
    private int fuelLevel;

    public FuelRecord(String vehicleId, int fuelLevel) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle id is required.");
        }
        validateFuelLevel(fuelLevel);

        this.vehicleId = vehicleId.trim();
        this.fuelLevel = fuelLevel;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(int fuelLevel) {
        validateFuelLevel(fuelLevel);
        this.fuelLevel = fuelLevel;
    }

    private void validateFuelLevel(int fuelLevel) {
        if (fuelLevel < 0 || fuelLevel > 100) {
            throw new IllegalArgumentException(
                    "Fuel level must be between 0 and 100."
            );
        }
    }
}
