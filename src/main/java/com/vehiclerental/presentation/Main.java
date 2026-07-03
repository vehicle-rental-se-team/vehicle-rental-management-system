package com.vehiclerental.presentation;

import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.exception.UnauthorizedAccessException;
import com.vehiclerental.repository.ManagerRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.AuthenticationService;
import com.vehiclerental.service.VehicleCatalogService;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ManagerRepository managerRepository = new ManagerRepository();
        VehicleRepository vehicleRepository = new VehicleRepository();
        AuthenticationService authenticationService = new AuthenticationService(managerRepository);
        VehicleCatalogService vehicleService = new VehicleCatalogService(vehicleRepository, authenticationService);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Vehicle Rental Management System");
        System.out.println("--------------------------------");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        boolean loginSuccess = authenticationService.login(username, password);

        if (!loginSuccess) {
            System.out.println("Invalid username or password.");
            scanner.close();
            return;
        }

        System.out.println("Login successful.");
        System.out.println();

        try {
            System.out.println("Available Vehicles:");
            List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles();

            for (Vehicle vehicle : availableVehicles) {
                System.out.println(vehicle);
            }
        } catch (UnauthorizedAccessException exception) {
            System.out.println(exception.getMessage());
        }

        authenticationService.logout();
        System.out.println();
        System.out.println("Logged out successfully.");
        scanner.close();
    }
}
