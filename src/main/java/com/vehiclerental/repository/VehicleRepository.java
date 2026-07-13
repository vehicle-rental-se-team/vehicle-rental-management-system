package com.vehiclerental.repository;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Motorcycle;
import com.vehiclerental.domain.Truck;
import com.vehiclerental.domain.Van;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
                    vehicles.add(convertLineToVehicle(line));
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not load vehicles from file: " + VEHICLES_FILE_PATH, exception);
        }
    }

    private Vehicle convertLineToVehicle(String line) {
        String[] parts = line.split(",");

        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid vehicle data: " + line);
        }

        String id = parts[0].trim();
        String type = parts[1].trim().toUpperCase();
        String brand = parts[2].trim();
        String model = parts[3].trim();
        double dailyRate = Double.parseDouble(parts[4].trim());
        VehicleStatus status = VehicleStatus.valueOf(parts[5].trim().toUpperCase());

        switch (type) {
            case "CAR":
                return new Car(id, brand, model, dailyRate, status);
            case "MOTORCYCLE":
                return new Motorcycle(id, brand, model, dailyRate, status);
            case "VAN":
                return new Van(id, brand, model, dailyRate, status);
            case "TRUCK":
                return new Truck(id, brand, model, dailyRate, status);
            case "ELECTRIC":
            case "ELECTRIC_VEHICLE":
                if (parts.length < 7) {
                    throw new IllegalArgumentException("Battery level is required for electric vehicle: " + line);
                }
                int batteryLevel = Integer.parseInt(parts[6].trim());
                return new ElectricVehicle(id, brand, model, dailyRate, status, batteryLevel);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
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
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }

        vehicles.add(vehicle);
        saveVehiclesToFile();
    }

    public void updateVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }

        if (!findById(vehicle.getId()).isPresent()) {
            throw new IllegalArgumentException("Vehicle does not exist: " + vehicle.getId());
        }

        saveVehiclesToFile();
    }

    private void saveVehiclesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLES_FILE_PATH))) {
            for (Vehicle vehicle : vehicles) {
                writer.write(convertVehicleToLine(vehicle));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not save vehicles to file: " + VEHICLES_FILE_PATH, exception);
        }
    }

    private String convertVehicleToLine(Vehicle vehicle) {
        String line = vehicle.getId()
                + "," + vehicle.getType()
                + "," + vehicle.getBrand()
                + "," + vehicle.getModel()
                + "," + vehicle.getDailyRate()
                + "," + vehicle.getStatus();

        if (vehicle instanceof ElectricVehicle) {
            ElectricVehicle electricVehicle = (ElectricVehicle) vehicle;
            line += "," + electricVehicle.getBatteryLevel();
        }

        return line;
    }
}
