package com.vehiclerental.repository;

import com.vehiclerental.domain.IncidentType;
import com.vehiclerental.domain.VehicleIncident;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleIncidentRepository {

    private static final String INCIDENTS_FILE_PATH = "data/incidents.txt";

    private final String filePath;
    private final List<VehicleIncident> incidents;

    public VehicleIncidentRepository() {
        this(INCIDENTS_FILE_PATH);
    }

    public VehicleIncidentRepository(String filePath) {
        this.filePath = filePath;
        this.incidents = new ArrayList<>();
        createIncidentsFileIfMissing();
        loadIncidentsFromFile();
    }

    public void save(VehicleIncident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("Incident is required.");
        }

        incidents.add(incident);
        saveAllIncidentsToFile();
    }

    public void update(VehicleIncident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("Incident is required.");
        }

        if (!findById(incident.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "Incident does not exist: " + incident.getId()
            );
        }

        saveAllIncidentsToFile();
    }

    public Optional<VehicleIncident> findById(String incidentId) {
        if (incidentId == null || incidentId.trim().isEmpty()) {
            return Optional.empty();
        }

        for (VehicleIncident incident : incidents) {
            if (incident.getId().equals(incidentId.trim())) {
                return Optional.of(incident);
            }
        }

        return Optional.empty();
    }

    public List<VehicleIncident> findByVehicleId(String vehicleId) {
        List<VehicleIncident> vehicleIncidents = new ArrayList<>();

        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return vehicleIncidents;
        }

        for (VehicleIncident incident : incidents) {
            if (incident.getVehicleId().equals(vehicleId.trim())) {
                vehicleIncidents.add(incident);
            }
        }

        return vehicleIncidents;
    }

    public List<VehicleIncident> findPendingAccidentsByVehicleId(String vehicleId) {
        List<VehicleIncident> pendingAccidents = new ArrayList<>();

        for (VehicleIncident incident : findByVehicleId(vehicleId)) {
            if (incident.requiresInspection()) {
                pendingAccidents.add(incident);
            }
        }

        return pendingAccidents;
    }

    public boolean hasPendingAccident(String vehicleId) {
        return !findPendingAccidentsByVehicleId(vehicleId).isEmpty();
    }

    public List<VehicleIncident> findAll() {
        return new ArrayList<>(incidents);
    }

    private void createIncidentsFileIfMissing() {
        File incidentsFile = new File(filePath);
        File parentDirectory = incidentsFile.getParentFile();

        try {
            if (parentDirectory != null && !parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }

            if (!incidentsFile.exists()) {
                incidentsFile.createNewFile();
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not create incidents file: " + filePath,
                    exception
            );
        }
    }

    private void loadIncidentsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    incidents.add(convertLineToIncident(line));
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not load incidents from file: " + filePath,
                    exception
            );
        }
    }

    private VehicleIncident convertLineToIncident(String line) {
        String[] parts = line.split(",", 6);

        if (parts.length < 5 || parts.length > 6) {
            throw new IllegalArgumentException("Invalid incident data: " + line);
        }

        IncidentType type = IncidentType.valueOf(parts[2].trim().toUpperCase());
        boolean inspectionCompleted = parts.length == 6
                ? Boolean.parseBoolean(parts[5].trim())
                : !IncidentType.ACCIDENT.equals(type);

        return new VehicleIncident(
                parts[0].trim(),
                parts[1].trim(),
                type,
                LocalDate.parse(parts[3].trim()),
                parts[4].trim(),
                inspectionCompleted
        );
    }

    private void saveAllIncidentsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (VehicleIncident incident : incidents) {
                writer.write(convertIncidentToLine(incident));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not save incidents to file: " + filePath,
                    exception
            );
        }
    }

    private String convertIncidentToLine(VehicleIncident incident) {
        return incident.getId() + ","
                + incident.getVehicleId() + ","
                + incident.getType() + ","
                + incident.getDate() + ","
                + cleanText(incident.getDescription()) + ","
                + incident.isInspectionCompleted();
    }

    private String cleanText(String value) {
        return value.replace(",", " ").trim();
    }
}
