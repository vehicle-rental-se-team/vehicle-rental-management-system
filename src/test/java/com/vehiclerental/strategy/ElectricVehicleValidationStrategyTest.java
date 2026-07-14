package com.vehiclerental.strategy;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.VehicleStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElectricVehicleValidationStrategyTest {

    @Test
    void shouldRejectLowBattery() {
        ElectricVehicleValidationStrategy strategy =
                new ElectricVehicleValidationStrategy();
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.UNAVAILABLE, 20);

        assertThrows(IllegalArgumentException.class, () ->
                strategy.validate(vehicle, new RentalRequest(25, false)));
    }

    @Test
    void shouldAllowEnoughBattery() {
        ElectricVehicleValidationStrategy strategy =
                new ElectricVehicleValidationStrategy();
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.AVAILABLE, 80);

        assertDoesNotThrow(() ->
                strategy.validate(vehicle, new RentalRequest(25, false)));
    }

    @Test
    void shouldRejectCustomerUnderEighteen() {
        ElectricVehicleValidationStrategy strategy =
                new ElectricVehicleValidationStrategy();
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.AVAILABLE, 80);

        assertThrows(IllegalArgumentException.class, () ->
                strategy.validate(vehicle, new RentalRequest(17, false)));
    }
}
