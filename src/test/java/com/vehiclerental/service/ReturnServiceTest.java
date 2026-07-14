package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReturnServiceTest {

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
    void shouldCloseRentalAndMakeVehicleAvailable() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        BillingService billingService = mock(BillingService.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        Rental rental = createRental();

        when(rentalRepository.findById("R1")).thenReturn(Optional.of(rental));
        when(billingService.calculateTotalCost(rental, LocalDate.of(2026, 7, 4)))
                .thenReturn(150.0);

        ReturnService service = new ReturnService(
                rentalRepository, billingService, authenticationService,
                null, vehicleRepository);

        double total = service.returnVehicle("R1", LocalDate.of(2026, 7, 4));

        assertEquals(150.0, total, 0.001);
        assertFalse(rental.isActive());
        assertEquals(VehicleStatus.AVAILABLE, rental.getVehicle().getStatus());
        verify(rentalRepository).update(rental);
        verify(vehicleRepository).updateVehicle(rental.getVehicle());
    }

    @Test
    void shouldRejectReturnBeforeRentalStartDate() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        Rental rental = createRental();
        when(rentalRepository.findById("R1")).thenReturn(Optional.of(rental));

        ReturnService service = new ReturnService(
                rentalRepository,
                mock(BillingService.class),
                mock(AuthenticationService.class));

        assertThrows(IllegalArgumentException.class, () ->
                service.returnVehicle("R1", LocalDate.of(2026, 6, 30)));
    }
}
