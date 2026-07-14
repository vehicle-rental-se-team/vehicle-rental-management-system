package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;

public class ElectricVehicleMonitoringService {

    private static final int LOW_BATTERY_LEVEL = 30;

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final NotificationPublisher notificationPublisher;
    private final String notificationRecipient;
    private final VehicleAvailabilityService availabilityService;

    public ElectricVehicleMonitoringService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            NotificationPublisher notificationPublisher,
            String notificationRecipient) {
        this(vehicleRepository, rentalRepository, notificationPublisher,
                notificationRecipient, null);
    }

    public ElectricVehicleMonitoringService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            NotificationPublisher notificationPublisher,
            String notificationRecipient,
            VehicleAvailabilityService availabilityService) {

        if (vehicleRepository == null) {
            throw new IllegalArgumentException("Vehicle repository is required.");
        }
        if (rentalRepository == null) {
            throw new IllegalArgumentException("Rental repository is required.");
        }
        if (notificationPublisher == null) {
            throw new IllegalArgumentException("Notification publisher is required.");
        }
        if (notificationRecipient == null || notificationRecipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification recipient is required.");
        }

        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.notificationPublisher = notificationPublisher;
        this.notificationRecipient = notificationRecipient;
        this.availabilityService = availabilityService;
    }

    public void updateBatteryLevel(String vehicleId, int batteryLevel) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        if (!(vehicle instanceof ElectricVehicle)) {
            throw new IllegalArgumentException("Vehicle is not electric: " + vehicleId);
        }

        ElectricVehicle electricVehicle = (ElectricVehicle) vehicle;
        boolean hasActiveRental = rentalRepository
                .findActiveRentalByVehicleId(vehicleId)
                .isPresent();

        electricVehicle.setBatteryLevel(batteryLevel);

        if (availabilityService != null) {
            availabilityService.applyStatus(electricVehicle, java.time.LocalDate.now());
        } else {
            updateVehicleStatus(electricVehicle, hasActiveRental);
        }
        sendBatteryNotification(electricVehicle, hasActiveRental);
        vehicleRepository.updateVehicle(electricVehicle);
    }

    public boolean isBatteryLow(ElectricVehicle vehicle) {
        return vehicle != null
                && vehicle.getBatteryLevel() > 0
                && vehicle.getBatteryLevel() < LOW_BATTERY_LEVEL;
    }

    public boolean isBatteryEmpty(ElectricVehicle vehicle) {
        return vehicle != null && vehicle.getBatteryLevel() == 0;
    }

    private void updateVehicleStatus(
            ElectricVehicle vehicle,
            boolean hasActiveRental) {

        if (hasActiveRental) {
            vehicle.setStatus(VehicleStatus.RENTED);
            return;
        }

        if (vehicle.getBatteryLevel() == 0) {
            vehicle.setStatus(VehicleStatus.UNAVAILABLE);
        } else if (vehicle.getBatteryLevel() >= LOW_BATTERY_LEVEL
                && VehicleStatus.UNAVAILABLE.equals(vehicle.getStatus())) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }
    }

    private void sendBatteryNotification(
            ElectricVehicle vehicle,
            boolean hasActiveRental) {

        if (isBatteryEmpty(vehicle)) {
            sendEmptyBatteryNotification(vehicle, hasActiveRental);
        } else if (isBatteryLow(vehicle)) {
            sendLowBatteryNotification(vehicle);
        }
    }

    private void sendLowBatteryNotification(ElectricVehicle vehicle) {
        String message = "Electric vehicle "
                + vehicle.getId()
                + " has low battery: "
                + vehicle.getBatteryLevel()
                + "%. Please charge it soon.";

        notificationPublisher.notifyObservers(notificationRecipient, message);
    }

    private void sendEmptyBatteryNotification(
            ElectricVehicle vehicle,
            boolean hasActiveRental) {

        String message = "Electric vehicle "
                + vehicle.getId()
                + " has an empty battery. Immediate charging is required.";

        if (!hasActiveRental) {
            message += " The vehicle is now unavailable.";
        }

        notificationPublisher.notifyObservers(notificationRecipient, message);
    }
}
