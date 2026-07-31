package com.vehiclerental.repository;

import com.vehiclerental.domain.FuelRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleFuelRepository {

    private static final String FUEL_FILE_PATH = "data/fuel.txt";

    private final String filePath;
    private final List<FuelRecord> records;

    public VehicleFuelRepository() {
        this(FUEL_FILE_PATH);
    }

    public VehicleFuelRepository(String filePath) {
        this.filePath = filePath;
        this.records = new ArrayList<>();
        createFileIfMissing();
        loadFromFile();
    }

    public Optional<FuelRecord> findByVehicleId(String vehicleId) {
        for (FuelRecord record : records) {
            if (record.getVehicleId().equals(vehicleId)) {
                return Optional.of(record);
            }
        }
        return Optional.empty();
    }

    public List<FuelRecord> findAll() {
        return new ArrayList<>(records);
    }

    public void saveOrUpdate(FuelRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Fuel record is required.");
        }

        Optional<FuelRecord> existing = findByVehicleId(record.getVehicleId());

        if (existing.isPresent()) {
            existing.get().setFuelLevel(record.getFuelLevel());
        } else {
            records.add(record);
        }

        saveToFile();
    }

    private void createFileIfMissing() {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not create fuel file.",
                    exception
            );
        }
    }

    private void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");

                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Invalid fuel data: " + line);
                    }

                    records.add(new FuelRecord(
                            parts[0].trim(),
                            Integer.parseInt(parts[1].trim())
                    ));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load fuel data.",
                    exception
            );
        }
    }

    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (FuelRecord record : records) {
                writer.write(record.getVehicleId() + "," + record.getFuelLevel());
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not save fuel data.",
                    exception
            );
        }
    }
}
