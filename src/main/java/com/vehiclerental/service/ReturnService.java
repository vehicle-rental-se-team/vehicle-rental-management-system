package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;

import java.time.LocalDate;
import java.util.Optional;

public class ReturnService {

    private final RentalRepository rentalRepository;
    private final BillingService billingService;
    private final AuthenticationService authenticationService;
    private final MaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleAvailabilityService availabilityService;

    public ReturnService(
            RentalRepository rentalRepository,
            BillingService billingService,
            AuthenticationService authenticationService
    ) {
        this(rentalRepository, billingService, authenticationService, null, null, null);
    }

    public ReturnService(
            RentalRepository rentalRepository,
            BillingService billingService,
            AuthenticationService authenticationService,
            MaintenanceRepository maintenanceRepository
    ) {
        this(rentalRepository, billingService, authenticationService,
                maintenanceRepository, null, null);
    }

    public ReturnService(
            RentalRepository rentalRepository,
            BillingService billingService,
            AuthenticationService authenticationService,
            MaintenanceRepository maintenanceRepository,
            VehicleRepository vehicleRepository
    ) {
        this(rentalRepository, billingService, authenticationService,
                maintenanceRepository, vehicleRepository, null);
    }

    public ReturnService(
            RentalRepository rentalRepository,
            BillingService billingService,
            AuthenticationService authenticationService,
            MaintenanceRepository maintenanceRepository,
            VehicleRepository vehicleRepository,
            VehicleAvailabilityService availabilityService
    ) {
        this.rentalRepository = rentalRepository;
        this.billingService = billingService;
        this.authenticationService = authenticationService;
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
        this.availabilityService = availabilityService;
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
        updateVehicleStatusAfterReturn(rental.getVehicle(), actualReturnDate);
        rentalRepository.update(rental);

        if (vehicleRepository != null) {
            vehicleRepository.updateVehicle(rental.getVehicle());
        }

        return totalCost;
    }

    private void updateVehicleStatusAfterReturn(
            Vehicle vehicle,
            LocalDate actualReturnDate) {
        if (availabilityService != null) {
            availabilityService.applyStatus(vehicle, actualReturnDate);
            return;
        }

        if (VehicleStatus.MAINTENANCE.equals(vehicle.getStatus())) {
            return;
        }

        if (maintenanceRepository != null
                && maintenanceRepository
                .findPendingByVehicleId(vehicle.getId())
                .filter(record -> !record.getNextMaintenanceDate()
                        .isAfter(actualReturnDate))
                .isPresent()) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
            return;
        }

        if (vehicle instanceof ElectricVehicle) {
            ElectricVehicle electricVehicle = (ElectricVehicle) vehicle;

            if (electricVehicle.getBatteryLevel() < 30) {
                vehicle.setStatus(VehicleStatus.UNAVAILABLE);
                return;
            }
        }

        vehicle.setStatus(VehicleStatus.AVAILABLE);
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
