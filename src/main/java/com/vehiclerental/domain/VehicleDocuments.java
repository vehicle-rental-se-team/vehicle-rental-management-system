package com.vehiclerental.domain;

import java.time.LocalDate;

public class VehicleDocuments {

    private final String vehicleId;
    private LocalDate registrationExpiryDate;
    private LocalDate insuranceExpiryDate;

    public VehicleDocuments(
            String vehicleId,
            LocalDate registrationExpiryDate,
            LocalDate insuranceExpiryDate) {

        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle id is required.");
        }
        if (registrationExpiryDate == null || insuranceExpiryDate == null) {
            throw new IllegalArgumentException("Document expiry dates are required.");
        }

        this.vehicleId = vehicleId.trim();
        this.registrationExpiryDate = registrationExpiryDate;
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public LocalDate getRegistrationExpiryDate() {
        return registrationExpiryDate;
    }

    public LocalDate getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public void updateDates(
            LocalDate registrationExpiryDate,
            LocalDate insuranceExpiryDate) {

        if (registrationExpiryDate == null || insuranceExpiryDate == null) {
            throw new IllegalArgumentException("Document expiry dates are required.");
        }

        this.registrationExpiryDate = registrationExpiryDate;
        this.insuranceExpiryDate = insuranceExpiryDate;
    }

    public boolean isRegistrationExpired(LocalDate date) {
        return !registrationExpiryDate.isAfter(date);
    }

    public boolean isInsuranceExpired(LocalDate date) {
        return !insuranceExpiryDate.isAfter(date);
    }
}
