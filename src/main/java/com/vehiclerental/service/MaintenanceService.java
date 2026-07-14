package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MaintenanceService {

    private static final int MAINTENANCE_INTERVAL_MONTHS = 6;
    private static final int REMINDER_DAYS_BEFORE = 5;

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final VehicleIncidentRepository incidentRepository;
    private final NotificationPublisher notificationPublisher;
    private final AuthenticationService authenticationService;
    private final String notificationRecipient;
    private final VehicleAvailabilityService availabilityService;

    public MaintenanceService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            MaintenanceRepository maintenanceRepository,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            String notificationRecipient) {

        this(
                vehicleRepository,
                rentalRepository,
                maintenanceRepository,
                null,
                notificationPublisher,
                authenticationService,
                notificationRecipient,
                null
        );
    }

    public MaintenanceService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            MaintenanceRepository maintenanceRepository,
            VehicleIncidentRepository incidentRepository,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            String notificationRecipient) {
        this(vehicleRepository, rentalRepository, maintenanceRepository,
                incidentRepository, notificationPublisher, authenticationService,
                notificationRecipient, null);
    }

    public MaintenanceService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            MaintenanceRepository maintenanceRepository,
            VehicleIncidentRepository incidentRepository,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            String notificationRecipient,
            VehicleAvailabilityService availabilityService) {

        if (vehicleRepository == null
                || rentalRepository == null
                || maintenanceRepository == null
                || notificationPublisher == null
                || authenticationService == null) {
            throw new IllegalArgumentException("Maintenance dependencies are required.");
        }
        if (notificationRecipient == null || notificationRecipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification recipient is required.");
        }

        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.incidentRepository = incidentRepository;
        this.notificationPublisher = notificationPublisher;
        this.authenticationService = authenticationService;
        this.notificationRecipient = notificationRecipient.trim();
        this.availabilityService = availabilityService;
    }

    public MaintenanceRecord scheduleMaintenance(
            String vehicleId,
            LocalDate lastMaintenanceDate) {

        authenticationService.requireLogin();
        validateVehicleId(vehicleId);

        if (lastMaintenanceDate == null) {
            throw new IllegalArgumentException("Last maintenance date is required.");
        }

        Vehicle vehicle = findVehicle(vehicleId);

        if (maintenanceRepository
                .findPendingByVehicleId(vehicle.getId())
                .isPresent()) {
            throw new IllegalStateException(
                    "Vehicle already has a pending maintenance schedule."
            );
        }

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID().toString(),
                vehicle.getId(),
                lastMaintenanceDate,
                lastMaintenanceDate.plusMonths(MAINTENANCE_INTERVAL_MONTHS),
                MaintenanceStatus.PENDING
        );

        maintenanceRepository.save(record);
        return record;
    }

    public int checkMaintenance(LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("Current date is required.");
        }

        int notificationsSent = 0;
        List<MaintenanceRecord> records = maintenanceRepository.findAll();

        for (MaintenanceRecord record : records) {
            if (!record.isPending()) {
                continue;
            }

            LocalDate nextDate = record.getNextMaintenanceDate();

            if (today.equals(nextDate.minusDays(REMINDER_DAYS_BEFORE))) {
                sendUpcomingMaintenanceNotification(record);
                notificationsSent++;
            }

            if (!today.isBefore(nextDate)) {
                markVehicleForMaintenance(record.getVehicleId());
                sendDueMaintenanceNotification(record, today);
                notificationsSent++;
            }
        }

        return notificationsSent;
    }

    public MaintenanceRecord completeMaintenance(
            String vehicleId,
            LocalDate completionDate) {

        authenticationService.requireLogin();
        validateVehicleId(vehicleId);

        if (completionDate == null) {
            throw new IllegalArgumentException("Completion date is required.");
        }

        Vehicle vehicle = findVehicle(vehicleId);
        MaintenanceRecord currentRecord = maintenanceRepository
                .findPendingByVehicleId(vehicle.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pending maintenance record not found for vehicle: "
                                + vehicle.getId()
                ));

        if (completionDate.isBefore(currentRecord.getLastMaintenanceDate())) {
            throw new IllegalArgumentException(
                    "Completion date cannot be before last maintenance date."
            );
        }

        currentRecord.complete();
        maintenanceRepository.update(currentRecord);

        MaintenanceRecord nextRecord = new MaintenanceRecord(
                UUID.randomUUID().toString(),
                vehicle.getId(),
                completionDate,
                completionDate.plusMonths(MAINTENANCE_INTERVAL_MONTHS),
                MaintenanceStatus.PENDING
        );
        maintenanceRepository.save(nextRecord);

        updateVehicleStatusAfterMaintenance(vehicle, completionDate);

        notificationPublisher.notifyObservers(
                notificationRecipient,
                "Maintenance completed for vehicle " + vehicle.getId()
                        + ". Next maintenance date is "
                        + nextRecord.getNextMaintenanceDate() + "."
        );

        return nextRecord;
    }

    public List<MaintenanceRecord> getAllMaintenanceRecords() {
        authenticationService.requireLogin();
        return maintenanceRepository.findAll();
    }

    private void markVehicleForMaintenance(String vehicleId) {
        Vehicle vehicle = findVehicle(vehicleId);

        if (rentalRepository
                .findActiveRentalByVehicleId(vehicleId)
                .isPresent()) {
            return;
        }

        if (!VehicleStatus.MAINTENANCE.equals(vehicle.getStatus())) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
            vehicleRepository.updateVehicle(vehicle);
        }
    }

    private void updateVehicleStatusAfterMaintenance(
            Vehicle vehicle,
            LocalDate completionDate) {
        if (availabilityService != null) {
            availabilityService.applyStatus(vehicle, completionDate);
            vehicleRepository.updateVehicle(vehicle);
            return;
        }

        if (rentalRepository
                .findActiveRentalByVehicleId(vehicle.getId())
                .isPresent()) {
            vehicle.setStatus(VehicleStatus.RENTED);
        } else if (incidentRepository != null
                && incidentRepository.hasPendingAccident(vehicle.getId())) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
        } else if (vehicle instanceof ElectricVehicle
                && ((ElectricVehicle) vehicle).getBatteryLevel() < 30) {
            vehicle.setStatus(VehicleStatus.UNAVAILABLE);
        } else {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }

        vehicleRepository.updateVehicle(vehicle);
    }

    private void sendUpcomingMaintenanceNotification(MaintenanceRecord record) {
        notificationPublisher.notifyObservers(
                notificationRecipient,
                "Vehicle " + record.getVehicleId()
                        + " requires maintenance on "
                        + record.getNextMaintenanceDate()
                        + ". Please prepare it for inspection."
        );
    }

    private void sendDueMaintenanceNotification(
            MaintenanceRecord record,
            LocalDate today) {

        String message;

        if (today.equals(record.getNextMaintenanceDate())) {
            message = "Vehicle " + record.getVehicleId()
                    + " is due for maintenance today and is unavailable.";
        } else {
            message = "Vehicle " + record.getVehicleId()
                    + " has overdue maintenance since "
                    + record.getNextMaintenanceDate() + ".";
        }

        notificationPublisher.notifyObservers(notificationRecipient, message);
    }

    private Vehicle findVehicle(String vehicleId) {
        return vehicleRepository.findById(vehicleId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + vehicleId
                ));
    }

    private void validateVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle id is required.");
        }
    }
}
