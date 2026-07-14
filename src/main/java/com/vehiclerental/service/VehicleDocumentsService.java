package com.vehiclerental.service;

import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleDocuments;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;

import java.time.LocalDate;
import java.util.List;

public class VehicleDocumentsService {

    private static final int REMINDER_DAYS_BEFORE = 5;

    private final VehicleRepository vehicleRepository;
    private final VehicleDocumentsRepository documentsRepository;
    private final VehicleAvailabilityService availabilityService;
    private final NotificationPublisher notificationPublisher;
    private final AuthenticationService authenticationService;
    private final String notificationRecipient;

    public VehicleDocumentsService(
            VehicleRepository vehicleRepository,
            VehicleDocumentsRepository documentsRepository,
            VehicleAvailabilityService availabilityService,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            String notificationRecipient) {

        if (vehicleRepository == null
                || documentsRepository == null
                || availabilityService == null
                || notificationPublisher == null
                || authenticationService == null) {
            throw new IllegalArgumentException("Vehicle document dependencies are required.");
        }
        if (notificationRecipient == null || notificationRecipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification recipient is required.");
        }

        this.vehicleRepository = vehicleRepository;
        this.documentsRepository = documentsRepository;
        this.availabilityService = availabilityService;
        this.notificationPublisher = notificationPublisher;
        this.authenticationService = authenticationService;
        this.notificationRecipient = notificationRecipient.trim();
    }

    public VehicleDocuments updateDocuments(
            String vehicleId,
            LocalDate registrationExpiryDate,
            LocalDate insuranceExpiryDate) {

        authenticationService.requireLogin();
        Vehicle vehicle = findVehicle(vehicleId);

        VehicleDocuments documents = documentsRepository
                .findByVehicleId(vehicle.getId())
                .orElse(new VehicleDocuments(
                        vehicle.getId(),
                        registrationExpiryDate,
                        insuranceExpiryDate
                ));

        documents.updateDates(registrationExpiryDate, insuranceExpiryDate);
        documentsRepository.saveOrUpdate(documents);

        availabilityService.applyStatus(vehicle, LocalDate.now());
        vehicleRepository.updateVehicle(vehicle);
        return documents;
    }

    public int checkDocuments(LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("Current date is required.");
        }

        int notifications = 0;
        List<VehicleDocuments> allDocuments = documentsRepository.findAll();

        for (VehicleDocuments documents : allDocuments) {
            Vehicle vehicle = findVehicle(documents.getVehicleId());

            if (today.equals(documents.getRegistrationExpiryDate()
                    .minusDays(REMINDER_DAYS_BEFORE))) {
                sendRegistrationReminder(documents);
                notifications++;
            }

            if (today.equals(documents.getInsuranceExpiryDate()
                    .minusDays(REMINDER_DAYS_BEFORE))) {
                sendInsuranceReminder(documents);
                notifications++;
            }

            if (documents.isRegistrationExpired(today)) {
                sendRegistrationExpired(documents);
                notifications++;
            }

            if (documents.isInsuranceExpired(today)) {
                sendInsuranceExpired(documents);
                notifications++;
            }

            availabilityService.applyStatus(vehicle, today);
            vehicleRepository.updateVehicle(vehicle);
        }

        return notifications;
    }

    public VehicleDocuments getDocuments(String vehicleId) {
        authenticationService.requireLogin();
        findVehicle(vehicleId);

        return documentsRepository.findByVehicleId(vehicleId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle documents not found: " + vehicleId
                ));
    }

    private void sendRegistrationReminder(VehicleDocuments documents) {
        notificationPublisher.notifyObservers(
                notificationRecipient,
                "Registration for vehicle " + documents.getVehicleId()
                        + " expires on "
                        + documents.getRegistrationExpiryDate() + "."
        );
    }

    private void sendInsuranceReminder(VehicleDocuments documents) {
        notificationPublisher.notifyObservers(
                notificationRecipient,
                "Insurance for vehicle " + documents.getVehicleId()
                        + " expires on "
                        + documents.getInsuranceExpiryDate() + "."
        );
    }

    private void sendRegistrationExpired(VehicleDocuments documents) {
        notificationPublisher.notifyObservers(
                notificationRecipient,
                "Registration for vehicle " + documents.getVehicleId()
                        + " has expired. The vehicle is unavailable."
        );
    }

    private void sendInsuranceExpired(VehicleDocuments documents) {
        notificationPublisher.notifyObservers(
                notificationRecipient,
                "Insurance for vehicle " + documents.getVehicleId()
                        + " has expired. The vehicle is unavailable."
        );
    }

    private Vehicle findVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle id is required.");
        }

        return vehicleRepository.findById(vehicleId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + vehicleId
                ));
    }
}
