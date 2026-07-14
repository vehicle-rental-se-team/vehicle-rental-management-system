package com.vehiclerental.domain;

import java.time.LocalDate;

public class VehicleIncident {

    private final String id;
    private final String vehicleId;
    private final IncidentType type;
    private final LocalDate date;
    private final String description;
    private boolean inspectionCompleted;

    public VehicleIncident(
            String id,
            String vehicleId,
            IncidentType type,
            LocalDate date,
            String description) {

        this(
                id,
                vehicleId,
                type,
                date,
                description,
                !IncidentType.ACCIDENT.equals(type)
        );
    }

    public VehicleIncident(
            String id,
            String vehicleId,
            IncidentType type,
            LocalDate date,
            String description,
            boolean inspectionCompleted) {

        this.id = id;
        this.vehicleId = vehicleId;
        this.type = type;
        this.date = date;
        this.description = description;
        this.inspectionCompleted = inspectionCompleted;
    }

    public String getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public IncidentType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInspectionCompleted() {
        return inspectionCompleted;
    }

    public boolean requiresInspection() {
        return IncidentType.ACCIDENT.equals(type) && !inspectionCompleted;
    }

    public void completeInspection() {
        if (IncidentType.ACCIDENT.equals(type)) {
            inspectionCompleted = true;
        }
    }
}
