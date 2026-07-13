package com.vehiclerental.strategy;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Vehicle;

public class ElectricVehicleValidationStrategy implements RentalValidationStrategy {
    private static final int MINIMUM_BATTERY_LEVEL = 30;

    @Override
    public void validate(Vehicle vehicle, RentalRequest request) {
        if (request.getCustomerAge() < 18) {
            throw new IllegalArgumentException("Customer must be at least 18 years old.");
        }

        ElectricVehicle electricVehicle = (ElectricVehicle) vehicle;
        if (electricVehicle.getBatteryLevel() < MINIMUM_BATTERY_LEVEL) {
            throw new IllegalArgumentException("Electric vehicle battery level must be at least 30%.");
        }
    }
}

