package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.Motorcycle;
import com.vehiclerental.domain.Truck;
import com.vehiclerental.domain.Van;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleRepository;

public class VehicleManagementService {

    private static final int MINIMUM_BATTERY_LEVEL = 30;
    private static final int MINIMUM_FUEL_LEVEL = 20;

    private final VehicleRepository vehicleRepository;
    private final VehicleFuelRepository fuelRepository;
    private final AuthenticationService authenticationService;

    public VehicleManagementService(
            VehicleRepository vehicleRepository,
            VehicleFuelRepository fuelRepository,
            AuthenticationService authenticationService) {

        if (vehicleRepository == null
                || fuelRepository == null
                || authenticationService == null) {
            throw new IllegalArgumentException(
                    "Vehicle management dependencies are required."
            );
        }

        this.vehicleRepository = vehicleRepository;
        this.fuelRepository = fuelRepository;
        this.authenticationService = authenticationService;
    }

    public Vehicle addVehicle(
            String id,
            String type,
            String brand,
            String model,
            double dailyRate,
            int energyLevel) {

        authenticationService.requireLogin();

        String vehicleId = requireText(id, "Vehicle id").toUpperCase();
        String vehicleType = requireText(type, "Vehicle type").toUpperCase();
        String vehicleBrand = requireText(brand, "Brand");
        String vehicleModel = requireText(model, "Model");

        if (vehicleRepository.findById(vehicleId).isPresent()) {
            throw new IllegalArgumentException(
                    "Vehicle id already exists: " + vehicleId
            );
        }
        if (Double.isNaN(dailyRate)
                || Double.isInfinite(dailyRate)
                || dailyRate <= 0) {
            throw new IllegalArgumentException(
                    "Daily rate must be greater than zero."
            );
        }
        if (energyLevel < 0 || energyLevel > 100) {
            throw new IllegalArgumentException(
                    "Battery or fuel level must be between 0 and 100."
            );
        }

        Vehicle vehicle = createVehicle(
                vehicleId,
                vehicleType,
                vehicleBrand,
                vehicleModel,
                dailyRate,
                energyLevel
        );

        vehicleRepository.addVehicle(vehicle);

        if (!(vehicle instanceof ElectricVehicle)) {
            fuelRepository.saveOrUpdate(
                    new FuelRecord(vehicle.getId(), energyLevel)
            );
        }

        return vehicle;
    }

    private Vehicle createVehicle(
            String id,
            String type,
            String brand,
            String model,
            double dailyRate,
            int energyLevel) {

        if ("ELECTRIC".equals(type)
                || "ELECTRIC_VEHICLE".equals(type)) {
            VehicleStatus status = energyLevel >= MINIMUM_BATTERY_LEVEL
                    ? VehicleStatus.AVAILABLE
                    : VehicleStatus.UNAVAILABLE;
            return new ElectricVehicle(
                    id, brand, model, dailyRate, status, energyLevel
            );
        }

        VehicleStatus status = energyLevel >= MINIMUM_FUEL_LEVEL
                ? VehicleStatus.AVAILABLE
                : VehicleStatus.UNAVAILABLE;

        switch (type) {
            case "CAR":
                return new Car(id, brand, model, dailyRate, status);
            case "MOTORCYCLE":
                return new Motorcycle(id, brand, model, dailyRate, status);
            case "VAN":
                return new Van(id, brand, model, dailyRate, status);
            case "TRUCK":
                return new Truck(id, brand, model, dailyRate, status);
            default:
                throw new IllegalArgumentException(
                        "Unsupported vehicle type: " + type
                );
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        String text = value.trim();
        if (text.contains(",")) {
            throw new IllegalArgumentException(
                    fieldName + " cannot contain commas."
            );
        }
        return text;
    }
}
