package com.vehiclerental.repository;

import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleRepository {

    private static final String VEHICLES_FILE_PATH = "data/vehicles.txt";

    private final List<Vehicle> vehicles;

    public VehicleRepository() {
        this.vehicles = new ArrayList<>();
        loadVehiclesFromFile();
    }

    private void loadVehiclesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(VEHICLES_FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Vehicle vehicle = convertLineToVehicle(line);
                    vehicles.add(vehicle);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not load vehicles from file: " + VEHICLES_FILE_PATH, exception);
        }
    }

    private Vehicle convertLineToVehicle(String line) {
        String[] parts = line.split(",");

        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid vehicle data: " + line);
        }

        String id = parts[0].trim();
        String brand = parts[1].trim();
        String model = parts[2].trim();
        double dailyRate = Double.parseDouble(parts[3].trim());
        VehicleStatus status = VehicleStatus.valueOf(parts[4].trim().toUpperCase());

        return new Vehicle(id, brand, model, dailyRate, status);
    }

    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicles);
    }

    public Optional<Vehicle> findById(String id) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId().equals(id)) {
                return Optional.of(vehicle);
            }
        }

        return Optional.empty();
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }
}