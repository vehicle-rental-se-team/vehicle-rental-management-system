package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleDocuments;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;

import java.time.LocalDate;

public class VehicleAvailabilityService {

    private static final int MINIMUM_BATTERY_LEVEL = 30;
    private static final int MINIMUM_FUEL_LEVEL = 20;

    private final RentalRepository rentalRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final VehicleIncidentRepository incidentRepository;
    private final VehicleFuelRepository fuelRepository;
    private final VehicleDocumentsRepository documentsRepository;

    public VehicleAvailabilityService(
            RentalRepository rentalRepository,
            MaintenanceRepository maintenanceRepository,
            VehicleIncidentRepository incidentRepository,
            VehicleFuelRepository fuelRepository,
            VehicleDocumentsRepository documentsRepository) {

        this.rentalRepository = rentalRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.incidentRepository = incidentRepository;
        this.fuelRepository = fuelRepository;
        this.documentsRepository = documentsRepository;
    }

    public VehicleStatus determineStatus(Vehicle vehicle, LocalDate date) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        if (rentalRepository != null
                && rentalRepository.findActiveRentalByVehicleId(vehicle.getId()).isPresent()) {
            return VehicleStatus.RENTED;
        }

        if (incidentRepository != null
                && incidentRepository.hasPendingAccident(vehicle.getId())) {
            return VehicleStatus.MAINTENANCE;
        }

        if (maintenanceRepository != null
                && maintenanceRepository.findPendingByVehicleId(vehicle.getId())
                .filter(record -> !record.getNextMaintenanceDate().isAfter(date))
                .isPresent()) {
            return VehicleStatus.MAINTENANCE;
        }

        if (vehicle instanceof ElectricVehicle
                && ((ElectricVehicle) vehicle).getBatteryLevel() < MINIMUM_BATTERY_LEVEL) {
            return VehicleStatus.UNAVAILABLE;
        }

        if (!(vehicle instanceof ElectricVehicle)
                && fuelRepository != null
                && fuelRepository.findByVehicleId(vehicle.getId())
                .map(FuelRecord::getFuelLevel)
                .filter(level -> level < MINIMUM_FUEL_LEVEL)
                .isPresent()) {
            return VehicleStatus.UNAVAILABLE;
        }

        if (documentsRepository != null) {
            VehicleDocuments documents = documentsRepository
                    .findByVehicleId(vehicle.getId())
                    .orElse(null);

            if (documents != null
                    && (documents.isRegistrationExpired(date)
                    || documents.isInsuranceExpired(date))) {
                return VehicleStatus.UNAVAILABLE;
            }
        }

        return VehicleStatus.AVAILABLE;
    }

    public VehicleStatus applyStatus(Vehicle vehicle, LocalDate date) {
        VehicleStatus status = determineStatus(vehicle, date);
        vehicle.setStatus(status);
        return status;
    }
}
