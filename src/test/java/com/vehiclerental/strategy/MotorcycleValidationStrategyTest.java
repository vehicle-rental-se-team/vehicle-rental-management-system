package com.vehiclerental.strategy;

import com.vehiclerental.domain.Motorcycle;
import com.vehiclerental.domain.VehicleStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MotorcycleValidationStrategyTest {

    @Test
    void shouldRejectCustomerUnderTwentyOne() {
        MotorcycleValidationStrategy strategy = new MotorcycleValidationStrategy();
        Motorcycle motorcycle = new Motorcycle(
                "M1", "Honda", "CBR", 35, VehicleStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () ->
                strategy.validate(motorcycle, new RentalRequest(20, false)));
    }

    @Test
    void shouldAllowCustomerAgedTwentyOne() {
        MotorcycleValidationStrategy strategy = new MotorcycleValidationStrategy();
        Motorcycle motorcycle = new Motorcycle(
                "M1", "Honda", "CBR", 35, VehicleStatus.AVAILABLE);

        assertDoesNotThrow(() ->
                strategy.validate(motorcycle, new RentalRequest(21, false)));
    }
}
