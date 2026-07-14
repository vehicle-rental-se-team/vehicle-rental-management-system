package com.vehiclerental.service;

import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.List;

public class VehicleCatalogService {

    private final VehicleRepository vehicleRepository;
    private final AuthenticationService authenticationService;

    public VehicleCatalogService (VehicleRepository vehicleRepository, AuthenticationService authenticationService) {
        this.vehicleRepository = vehicleRepository;
        this.authenticationService = authenticationService;
    }

    public List<Vehicle> getAvailableVehicles() {
        authenticationService.requireLogin();

        List<Vehicle> availableVehicles = new ArrayList<>();

        for (Vehicle vehicle : vehicleRepository.findAll()) {
            if (vehicle.isAvailable()) {
                availableVehicles.add(vehicle);
            }
        }

        return availableVehicles;
    }

    public List<Vehicle> getAllVehicles() {
        authenticationService.requireLogin();
        return vehicleRepository.findAll();
    }
}
