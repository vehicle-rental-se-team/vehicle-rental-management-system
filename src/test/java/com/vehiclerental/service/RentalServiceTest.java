package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.exception.InvalidRentalPeriodException;
import com.vehiclerental.exception.RentalAlreadyExistsException;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalServiceTest {

    @Test
    void shouldCreateRentalAndChangeVehicleStatus() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        RentalRepository rentalRepository = mock(RentalRepository.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());

        RentalService service = new RentalService(
                vehicleRepository, rentalRepository, authenticationService);

        Rental rental = service.rentVehicle(
                "V1", "Ahmad", "ahmad@test.com", 25, false,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 5));

        assertNotNull(rental);
        assertEquals(VehicleStatus.RENTED, car.getStatus());
        verify(rentalRepository).save(any(Rental.class));
        verify(vehicleRepository).updateVehicle(car);
    }

    @Test
    void shouldPreventDoubleBooking() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        RentalRepository rentalRepository = mock(RentalRepository.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        Rental existingRental = mock(Rental.class);

        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.of(existingRental));

        RentalService service = new RentalService(
                vehicleRepository, rentalRepository, authenticationService);

        assertThrows(RentalAlreadyExistsException.class, () ->
                service.rentVehicle(
                        "V1", "Ahmad", "ahmad@test.com", 25, false,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 5)));
    }

    @Test
    void shouldRejectRentalLongerThanThirtyDays() {
        RentalService service = new RentalService(
                mock(VehicleRepository.class),
                mock(RentalRepository.class),
                mock(AuthenticationService.class));

        assertThrows(InvalidRentalPeriodException.class, () ->
                service.rentVehicle(
                        "V1", "Ahmad", "ahmad@test.com", 25, false,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 8, 2)));
    }

    @Test
    void shouldRejectUnavailableVehicle() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        Car car = new Car(
                "V1", "Toyota", "Corolla", 50,
                VehicleStatus.UNAVAILABLE
        );
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));

        RentalService service = new RentalService(
                vehicleRepository,
                mock(RentalRepository.class),
                mock(AuthenticationService.class)
        );

        assertThrows(RuntimeException.class, () ->
                service.rentVehicle(
                        "V1", "Ahmad", "ahmad@test.com", 25, false,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 5)
                ));
    }

    @Test
    void shouldRejectUnknownVehicle() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        when(vehicleRepository.findById("V9")).thenReturn(Optional.empty());

        RentalService service = new RentalService(
                vehicleRepository,
                mock(RentalRepository.class),
                mock(AuthenticationService.class)
        );

        assertThrows(RuntimeException.class, () ->
                service.rentVehicle(
                        "V9", "Ahmad", "ahmad@test.com", 25, false,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 5)
                ));
    }

    @Test
    void shouldRejectInvalidCustomerEmail() {
        RentalService service = new RentalService(
                mock(VehicleRepository.class),
                mock(RentalRepository.class),
                mock(AuthenticationService.class)
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.rentVehicle(
                        "V1", "Ahmad", "invalid-email", 25, false,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 5)
                ));
    }

    @Test
    void shouldRejectEndDateEqualToStartDate() {
        RentalService service = new RentalService(
                mock(VehicleRepository.class),
                mock(RentalRepository.class),
                mock(AuthenticationService.class)
        );

        assertThrows(InvalidRentalPeriodException.class, () ->
                service.rentVehicle(
                        "V1", "Ahmad", "ahmad@test.com", 25, false,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 1)
                ));
    }

}
