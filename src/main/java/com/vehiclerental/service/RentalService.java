package com.vehiclerental.service;

import com.vehiclerental.domain.Customer;
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
import java.util.UUID;

public class RentalService {

    private static final int MAX_RENTAL_DAYS = 30;

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final AuthenticationService authenticationService;

    public RentalService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            AuthenticationService authenticationService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.authenticationService = authenticationService;
    }

    public Rental rentVehicle(
            String vehicleId,
            String customerName,
            String customerEmail,
            LocalDate startDate,
            LocalDate endDate
    ) {
        authenticationService.requireLogin();

        validateVehicleId(vehicleId);
        validateCustomer(customerName, customerEmail);
        validateRentalPeriod(startDate, endDate);

        Vehicle vehicle = vehicleRepository.findById(vehicleId.trim())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));

        if (!vehicle.isAvailable()) {
            throw new VehicleNotAvailableException("Vehicle is not available for rental.");
        }

        if (rentalRepository.findActiveRentalByVehicleId(vehicleId.trim()).isPresent()) {
            throw new RentalAlreadyExistsException("Vehicle already has an active rental.");
        }

        Customer customer = new Customer(
                UUID.randomUUID().toString(),
                customerName.trim(),
                customerEmail.trim()
        );

        Rental rental = new Rental(
                UUID.randomUUID().toString(),
                vehicle,
                customer,
                startDate,
                endDate
        );

        rentalRepository.save(rental);
        vehicle.setStatus(VehicleStatus.RENTED);

        return rental;
    }

    private void validateVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle id is required.");
        }
    }

    private void validateCustomer(String customerName, String customerEmail) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required.");
        }
        if (!customerEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Customer email is invalid.");
        }
    }

    private void validateRentalPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidRentalPeriodException("Start date and end date are required.");
        }

        if (!endDate.isAfter(startDate)) {
            throw new InvalidRentalPeriodException("End date must be after start date.");
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);

        if (rentalDays > MAX_RENTAL_DAYS) {
            throw new InvalidRentalPeriodException(
                    "Rental duration cannot exceed " + MAX_RENTAL_DAYS + " days."
            );
        }
    }
}
