package com.vehiclerental.repository;

import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;

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

public class MaintenanceRepository {

    private static final String MAINTENANCE_FILE_PATH = "data/maintenance.txt";

    private final List<MaintenanceRecord> records;

    public MaintenanceRepository() {
        this.records = new ArrayList<>();
        createMaintenanceFileIfMissing();
        loadRecordsFromFile();
    }

    public void save(MaintenanceRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Maintenance record is required.");
        }

        records.add(record);
        saveAllRecordsToFile();
    }

    public void update(MaintenanceRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Maintenance record is required.");
        }

        saveAllRecordsToFile();
    }

    public Optional<MaintenanceRecord> findPendingByVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            return Optional.empty();
        }

        for (MaintenanceRecord record : records) {
            if (record.isPending()
                    && record.getVehicleId().equals(vehicleId.trim())) {
                return Optional.of(record);
            }
        }

        return Optional.empty();
    }

    public List<MaintenanceRecord> findAll() {
        return new ArrayList<>(records);
    }

    private void createMaintenanceFileIfMissing() {
        File maintenanceFile = new File(MAINTENANCE_FILE_PATH);
        File parentDirectory = maintenanceFile.getParentFile();

        try {
            if (parentDirectory != null && !parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }

            if (!maintenanceFile.exists()) {
                maintenanceFile.createNewFile();
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not create maintenance file: " + MAINTENANCE_FILE_PATH,
                    exception
            );
        }
    }

    private void loadRecordsFromFile() {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(MAINTENANCE_FILE_PATH))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    records.add(convertLineToRecord(line));
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not load maintenance records from file: "
                            + MAINTENANCE_FILE_PATH,
                    exception
            );
        }
    }

    private MaintenanceRecord convertLineToRecord(String line) {
        String[] parts = line.split(",", -1);

        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid maintenance data: " + line);
        }

        return new MaintenanceRecord(
                parts[0].trim(),
                parts[1].trim(),
                LocalDate.parse(parts[2].trim()),
                LocalDate.parse(parts[3].trim()),
                MaintenanceStatus.valueOf(parts[4].trim().toUpperCase())
        );
    }

    private void saveAllRecordsToFile() {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(MAINTENANCE_FILE_PATH))) {

            for (MaintenanceRecord record : records) {
                writer.write(convertRecordToLine(record));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not save maintenance records to file: "
                            + MAINTENANCE_FILE_PATH,
                    exception
            );
        }
    }

    private String convertRecordToLine(MaintenanceRecord record) {
        return record.getId() + ","
                + record.getVehicleId() + ","
                + record.getLastMaintenanceDate() + ","
                + record.getNextMaintenanceDate() + ","
                + record.getStatus();
    }
}
