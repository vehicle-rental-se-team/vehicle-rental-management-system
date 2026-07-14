package com.vehiclerental.repository;

import com.vehiclerental.domain.VehicleDocuments;

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

public class VehicleDocumentsRepository {

    private static final String DOCUMENTS_FILE_PATH =
            "data/vehicle_documents.txt";

    private final List<VehicleDocuments> documents;

    public VehicleDocumentsRepository() {
        documents = new ArrayList<>();
        createFileIfMissing();
        loadFromFile();
    }

    public Optional<VehicleDocuments> findByVehicleId(String vehicleId) {
        for (VehicleDocuments item : documents) {
            if (item.getVehicleId().equals(vehicleId)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public List<VehicleDocuments> findAll() {
        return new ArrayList<>(documents);
    }

    public void saveOrUpdate(VehicleDocuments item) {
        if (item == null) {
            throw new IllegalArgumentException("Vehicle documents are required.");
        }

        Optional<VehicleDocuments> existing = findByVehicleId(item.getVehicleId());

        if (existing.isPresent()) {
            existing.get().updateDates(
                    item.getRegistrationExpiryDate(),
                    item.getInsuranceExpiryDate()
            );
        } else {
            documents.add(item);
        }

        saveToFile();
    }

    private void createFileIfMissing() {
        try {
            File file = new File(DOCUMENTS_FILE_PATH);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not create documents file.", exception);
        }
    }

    private void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DOCUMENTS_FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");

                    if (parts.length != 3) {
                        throw new IllegalArgumentException(
                                "Invalid vehicle documents data: " + line
                        );
                    }

                    documents.add(new VehicleDocuments(
                            parts[0].trim(),
                            LocalDate.parse(parts[1].trim()),
                            LocalDate.parse(parts[2].trim())
                    ));
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not load vehicle documents.", exception);
        }
    }

    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOCUMENTS_FILE_PATH))) {
            for (VehicleDocuments item : documents) {
                writer.write(item.getVehicleId()
                        + "," + item.getRegistrationExpiryDate()
                        + "," + item.getInsuranceExpiryDate());
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not save vehicle documents.", exception);
        }
    }
}
