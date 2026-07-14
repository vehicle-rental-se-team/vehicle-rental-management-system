package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.IncidentType;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleIncident;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class VehicleIncidentService {

    private final VehicleRepository vehicleRepository;
    private final VehicleIncidentRepository incidentRepository;
    private final NotificationPublisher notificationPublisher;
    private final AuthenticationService authenticationService;
    private final RentalRepository rentalRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final String notificationRecipient;
    private final VehicleAvailabilityService availabilityService;

    public VehicleIncidentService(
            VehicleRepository vehicleRepository,
            VehicleIncidentRepository incidentRepository,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            String notificationRecipient) {

        this(
                vehicleRepository,
                incidentRepository,
                notificationPublisher,
                authenticationService,
                null,
                null,
                notificationRecipient,
                null
        );
    }

    public VehicleIncidentService(
            VehicleRepository vehicleRepository,
            VehicleIncidentRepository incidentRepository,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            RentalRepository rentalRepository,
            MaintenanceRepository maintenanceRepository,
            String notificationRecipient) {
        this(vehicleRepository, incidentRepository, notificationPublisher,
                authenticationService, rentalRepository, maintenanceRepository,
                notificationRecipient, null);
    }

    public VehicleIncidentService(
            VehicleRepository vehicleRepository,
            VehicleIncidentRepository incidentRepository,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            RentalRepository rentalRepository,
            MaintenanceRepository maintenanceRepository,
            String notificationRecipient,
            VehicleAvailabilityService availabilityService) {

        if (vehicleRepository == null) {
            throw new IllegalArgumentException("Vehicle repository is required.");
        }
        if (incidentRepository == null) {
            throw new IllegalArgumentException("Incident repository is required.");
        }
        if (notificationPublisher == null) {
            throw new IllegalArgumentException("Notification publisher is required.");
        }
        if (authenticationService == null) {
            throw new IllegalArgumentException("Authentication service is required.");
        }
        if (notificationRecipient == null || notificationRecipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification recipient is required.");
        }

        this.vehicleRepository = vehicleRepository;
        this.incidentRepository = incidentRepository;
        this.notificationPublisher = notificationPublisher;
        this.authenticationService = authenticationService;
        this.rentalRepository = rentalRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.notificationRecipient = notificationRecipient.trim();
        this.availabilityService = availabilityService;
    }

    public VehicleIncident recordIncident(
            String vehicleId,
            IncidentType type,
            LocalDate date,
            String description) {

        authenticationService.requireLogin();
        validateIncidentData(vehicleId, type, date, description);

        Vehicle vehicle = findVehicle(vehicleId);

        VehicleIncident incident = new VehicleIncident(
                UUID.randomUUID().toString(),
                vehicle.getId(),
                type,
                date,
                description.trim()
        );

        incidentRepository.save(incident);

        if (IncidentType.ACCIDENT.equals(type)) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
            vehicleRepository.updateVehicle(vehicle);
        }

        sendIncidentNotification(vehicle, incident);
        return incident;
    }

    public int completeInspection(
            String vehicleId,
            LocalDate inspectionDate) {

        authenticationService.requireLogin();
        validateVehicleId(vehicleId);

        if (inspectionDate == null) {
            throw new IllegalArgumentException("Inspection date is required.");
        }

        Vehicle vehicle = findVehicle(vehicleId);
        List<VehicleIncident> pendingAccidents =
                incidentRepository.findPendingAccidentsByVehicleId(vehicle.getId());

        if (pendingAccidents.isEmpty()) {
            throw new IllegalStateException(
                    "No pending accident inspection found for vehicle: "
                            + vehicle.getId()
            );
        }

        for (VehicleIncident accident : pendingAccidents) {
            accident.completeInspection();
            incidentRepository.update(accident);
        }

        updateVehicleStatusAfterInspection(vehicle, inspectionDate);

        notificationPublisher.notifyObservers(
                notificationRecipient,
                "Inspection completed for vehicle " + vehicle.getId()
                        + " on " + inspectionDate + "."
        );

        return pendingAccidents.size();
    }

    public List<VehicleIncident> getPendingAccidents(String vehicleId) {
        authenticationService.requireLogin();
        validateVehicleId(vehicleId);
        return incidentRepository.findPendingAccidentsByVehicleId(vehicleId.trim());
    }

    public List<VehicleIncident> getVehicleIncidents(String vehicleId) {
        authenticationService.requireLogin();
        validateVehicleId(vehicleId);
        return incidentRepository.findByVehicleId(vehicleId.trim());
    }

    public List<VehicleIncident> getAllIncidents() {
        authenticationService.requireLogin();
        return incidentRepository.findAll();
    }

    private void updateVehicleStatusAfterInspection(
            Vehicle vehicle,
            LocalDate inspectionDate) {

        if (availabilityService != null) {
            availabilityService.applyStatus(vehicle, inspectionDate);
            vehicleRepository.updateVehicle(vehicle);
            return;
        }

        if (rentalRepository != null
                && rentalRepository
                .findActiveRentalByVehicleId(vehicle.getId())
                .isPresent()) {
            vehicle.setStatus(VehicleStatus.RENTED);
        } else if (maintenanceRepository != null
                && maintenanceRepository
                .findPendingByVehicleId(vehicle.getId())
                .filter(record -> !record.getNextMaintenanceDate()
                        .isAfter(inspectionDate))
                .isPresent()) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
        } else if (vehicle instanceof ElectricVehicle
                && ((ElectricVehicle) vehicle).getBatteryLevel() < 30) {
            vehicle.setStatus(VehicleStatus.UNAVAILABLE);
        } else {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }

        vehicleRepository.updateVehicle(vehicle);
    }

    private void validateIncidentData(
            String vehicleId,
            IncidentType type,
            LocalDate date,
            String description) {

        validateVehicleId(vehicleId);

        if (type == null) {
            throw new IllegalArgumentException("Incident type is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Incident date is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Incident description is required.");
        }
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

    private void sendIncidentNotification(
            Vehicle vehicle,
            VehicleIncident incident) {

        String message;

        if (IncidentType.ACCIDENT.equals(incident.getType())) {
            message = "Vehicle " + vehicle.getId()
                    + " was involved in an accident on " + incident.getDate()
                    + ". It is now under maintenance. Details: "
                    + incident.getDescription();
        } else {
            message = "Vehicle " + vehicle.getId()
                    + " received a violation on " + incident.getDate()
                    + ". Details: " + incident.getDescription();
        }

        notificationPublisher.notifyObservers(notificationRecipient, message);
    }
}
