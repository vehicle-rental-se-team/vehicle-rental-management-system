package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;

import java.time.LocalDate;

public class FuelMonitoringService {

    private static final int LOW_FUEL_LEVEL = 20;

    private final VehicleRepository vehicleRepository;
    private final VehicleFuelRepository fuelRepository;
    private final VehicleAvailabilityService availabilityService;
    private final NotificationPublisher notificationPublisher;
    private final AuthenticationService authenticationService;
    private final String notificationRecipient;

    public FuelMonitoringService(
            VehicleRepository vehicleRepository,
            VehicleFuelRepository fuelRepository,
            VehicleAvailabilityService availabilityService,
            NotificationPublisher notificationPublisher,
            AuthenticationService authenticationService,
            String notificationRecipient) {

        if (vehicleRepository == null
                || fuelRepository == null
                || availabilityService == null
                || notificationPublisher == null
                || authenticationService == null) {
            throw new IllegalArgumentException("Fuel monitoring dependencies are required.");
        }
        if (notificationRecipient == null || notificationRecipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification recipient is required.");
        }

        this.vehicleRepository = vehicleRepository;
        this.fuelRepository = fuelRepository;
        this.availabilityService = availabilityService;
        this.notificationPublisher = notificationPublisher;
        this.authenticationService = authenticationService;
        this.notificationRecipient = notificationRecipient.trim();
    }

    public FuelRecord updateFuelLevel(String vehicleId, int fuelLevel) {
        return updateFuelLevel(vehicleId, fuelLevel, LocalDate.now());
    }

    public FuelRecord updateFuelLevel(
            String vehicleId,
            int fuelLevel,
            LocalDate date) {

        authenticationService.requireLogin();

        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        Vehicle vehicle = findVehicle(vehicleId);

        if (vehicle instanceof ElectricVehicle) {
            throw new IllegalArgumentException(
                    "Electric vehicles do not use fuel: " + vehicleId
            );
        }

        FuelRecord record = fuelRepository.findByVehicleId(vehicle.getId())
                .orElse(new FuelRecord(vehicle.getId(), fuelLevel));
        record.setFuelLevel(fuelLevel);
        fuelRepository.saveOrUpdate(record);

        availabilityService.applyStatus(vehicle, date);
        vehicleRepository.updateVehicle(vehicle);
        sendFuelNotification(vehicle, record);

        return record;
    }

    public int getFuelLevel(String vehicleId) {
        authenticationService.requireLogin();
        findVehicle(vehicleId);

        return fuelRepository.findByVehicleId(vehicleId.trim())
                .map(FuelRecord::getFuelLevel)
                .orElse(100);
    }

    private void sendFuelNotification(Vehicle vehicle, FuelRecord record) {
        if (record.getFuelLevel() == 0) {
            notificationPublisher.notifyObservers(
                    notificationRecipient,
                    "Vehicle " + vehicle.getId()
                            + " has no fuel. Refueling is required immediately."
            );
        } else if (record.getFuelLevel() < LOW_FUEL_LEVEL) {
            notificationPublisher.notifyObservers(
                    notificationRecipient,
                    "Vehicle " + vehicle.getId()
                            + " has low fuel: " + record.getFuelLevel()
                            + "%. Please refuel it soon."
            );
        }
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
