package com.vehiclerental.strategy;

import com.vehiclerental.domain.Truck;
import com.vehiclerental.domain.VehicleStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TruckValidationStrategyTest {

    @Test
    void shouldRejectTruckWithoutSpecialLicense() {
        TruckValidationStrategy strategy = new TruckValidationStrategy();
        Truck truck = new Truck("T1", "Volvo", "FH", 150, VehicleStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () ->
                strategy.validate(truck, new RentalRequest(25, false)));
    }

    @Test
    void shouldAllowTruckWithSpecialLicense() {
        TruckValidationStrategy strategy = new TruckValidationStrategy();
        Truck truck = new Truck("T1", "Volvo", "FH", 150, VehicleStatus.AVAILABLE);

        assertDoesNotThrow(() ->
                strategy.validate(truck, new RentalRequest(25, true)));
    }
}
