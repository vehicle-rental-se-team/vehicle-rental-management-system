package com.vehiclerental.domain;

import java.time.LocalDate;

public class MaintenanceRecord {

    private final String id;
    private final String vehicleId;
    private final LocalDate lastMaintenanceDate;
    private final LocalDate nextMaintenanceDate;
    private MaintenanceStatus status;

    public MaintenanceRecord(
            String id,
            String vehicleId,
            LocalDate lastMaintenanceDate,
            LocalDate nextMaintenanceDate,
            MaintenanceStatus status) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Maintenance id is required.");
        }
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle id is required.");
        }
        if (lastMaintenanceDate == null || nextMaintenanceDate == null) {
            throw new IllegalArgumentException("Maintenance dates are required.");
        }
        if (!nextMaintenanceDate.isAfter(lastMaintenanceDate)) {
            throw new IllegalArgumentException(
                    "Next maintenance date must be after last maintenance date."
            );
        }
        if (status == null) {
            throw new IllegalArgumentException("Maintenance status is required.");
        }

        this.id = id.trim();
        this.vehicleId = vehicleId.trim();
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public LocalDate getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void complete() {
        status = MaintenanceStatus.COMPLETED;
    }

    public boolean isPending() {
        return MaintenanceStatus.PENDING.equals(status);
    }
}
