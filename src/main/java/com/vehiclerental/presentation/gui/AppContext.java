package com.vehiclerental.presentation.gui;

import com.vehiclerental.repository.ManagerRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.AuthenticationService;
import com.vehiclerental.service.VehicleCatalogService;

public class AppContext {

    private final ManagerRepository managerRepository;
    private final VehicleRepository vehicleRepository;
    private final AuthenticationService authenticationService;
    private final VehicleCatalogService vehicleCatalogService;

    public AppContext() {
        this.managerRepository = new ManagerRepository();
        this.vehicleRepository = new VehicleRepository();
        this.authenticationService = new AuthenticationService(managerRepository);
        this.vehicleCatalogService = new VehicleCatalogService(vehicleRepository, authenticationService);
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public VehicleCatalogService getVehicleCatalogService() {
        return vehicleCatalogService;
    }
}
