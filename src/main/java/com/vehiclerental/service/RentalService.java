package com.vehiclerental.service;

import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.exception.InvalidRentalPeriodException;
import com.vehiclerental.exception.RentalAlreadyExistsException;
import com.vehiclerental.exception.VehicleNotAvailableException;
import com.vehiclerental.exception.VehicleNotFoundException;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class RentalService {

    private static final long MIN_RENTAL_DAYS = 1;
    private static final long MAX_RENTAL_DAYS = 30;

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final AuthenticationService authenticationService;

    public RentalService(VehicleRepository vehicleRepository,
                         RentalRepository rentalRepository,
                         AuthenticationService authenticationService) {
        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.authenticationService = authenticationService;
    }

    public Rental rentVehicle(String vehicleId, String customerName, LocalDate startDate, LocalDate endDate) {
        authenticationService.requireLogin();
        validateRentalPeriod(startDate, endDate);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle was not found: " + vehicleId));

        Optional<Rental> activeRental = rentalRepository.findActiveRentalByVehicleId(vehicleId);

        if (activeRental.isPresent()) {
            throw new RentalAlreadyExistsException("Vehicle already has an active rental: " + vehicleId);
        }

        if (!vehicle.isAvailable()) {
            throw new VehicleNotAvailableException("Vehicle is not available for rent: " + vehicleId);
        }

        String rentalId = createRentalId(vehicleId);
        Rental rental = new Rental(rentalId, vehicle, customerName, startDate, endDate);

        rentalRepository.save(rental);
        vehicle.setStatus(VehicleStatus.RENTED);

        return rental;
    }

    private void validateRentalPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidRentalPeriodException("Rental start date and end date are required.");
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);

        if (rentalDays < MIN_RENTAL_DAYS) {
            throw new InvalidRentalPeriodException("Rental period must be at least one day.");
        }

        if (rentalDays > MAX_RENTAL_DAYS) {
            throw new InvalidRentalPeriodException("Rental period cannot be more than 30 days.");
        }
    }

    private String createRentalId(String vehicleId) {
        return "R-" + vehicleId + "-" + System.currentTimeMillis();
    }
}
