package com.vehiclerental.strategy;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.VehicleStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StandardValidationStrategyTest {

    @Test
    void shouldAllowAdultCustomer() {
        StandardValidationStrategy strategy = new StandardValidationStrategy();
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        assertDoesNotThrow(() ->
                strategy.validate(car, new RentalRequest(18, false)));
    }

    @Test
    void shouldRejectCustomerUnderEighteen() {
        StandardValidationStrategy strategy = new StandardValidationStrategy();
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () ->
                strategy.validate(car, new RentalRequest(17, false)));
    }
}
