package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private Rental createRental() {
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.RENTED);
        Customer customer = new Customer("C1", "Ahmad", "ahmad@test.com");
        return new Rental(
                "R1", car, customer,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 4)
        );
    }

    @Test
    void shouldCalculateBaseCostUsingRentalDays() {
        BillingService service = new BillingService(20);

        assertEquals(150.0, service.calculateBaseCost(createRental()), 0.001);
    }

    @Test
    void shouldCalculateLatePenalty() {
        BillingService service = new BillingService(20);
        LocalDate actualReturnDate = LocalDate.of(2026, 7, 6);

        assertEquals(2, service.calculateLateDays(createRental(), actualReturnDate));
        assertEquals(40.0, service.calculateLatePenalty(createRental(), actualReturnDate), 0.001);
        assertEquals(190.0, service.calculateTotalCost(createRental(), actualReturnDate), 0.001);
    }

    @Test
    void shouldNotAddPenaltyWhenReturnedOnTime() {
        BillingService service = new BillingService(20);

        assertEquals(0, service.calculateLateDays(
                createRental(), LocalDate.of(2026, 7, 4)));
    }
}
