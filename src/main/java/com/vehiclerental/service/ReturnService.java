package com.vehiclerental.service;

import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.RentalRepository;

import java.time.LocalDate;
import java.util.Optional;

public class ReturnService {

    private final RentalRepository rentalRepository;
    private final BillingService billingService;
    private final AuthenticationService authenticationService;

    public ReturnService(
            RentalRepository rentalRepository,
            BillingService billingService,
            AuthenticationService authenticationService
    ) {
        this.rentalRepository = rentalRepository;
        this.billingService = billingService;
        this.authenticationService = authenticationService;
    }

    public double returnVehicle(String rentalId, LocalDate actualReturnDate) {
        authenticationService.requireLogin();

        if (actualReturnDate == null) {
            throw new IllegalArgumentException("Actual return date is required.");
        }

        Rental rental = findActiveRental(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Active rental not found."));

        if (actualReturnDate.isBefore(rental.getStartDate())) {
            throw new IllegalArgumentException("Return date cannot be before rental start date.");
        }

        double totalCost = billingService.calculateTotalCost(rental, actualReturnDate);

        rental.close();
        rental.getVehicle().setStatus(VehicleStatus.AVAILABLE);

        return totalCost;
    }

    public Optional<Rental> findActiveRental(String rentalId) {
        authenticationService.requireLogin();

        if (rentalId == null || rentalId.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<Rental> rental = rentalRepository.findById(rentalId.trim());

        if (!rental.isPresent() || !rental.get().isActive()) {
            return Optional.empty();
        }

        return rental;
    }
}
