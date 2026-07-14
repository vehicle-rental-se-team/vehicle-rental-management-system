package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleDocuments;
import com.vehiclerental.domain.VehicleIncident;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleRepository;

import java.util.ArrayList;
import java.util.List;

public class VehicleHistoryService {

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final VehicleIncidentRepository incidentRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final VehicleFuelRepository fuelRepository;
    private final VehicleDocumentsRepository documentsRepository;
    private final AuthenticationService authenticationService;

    public VehicleHistoryService(
            VehicleRepository vehicleRepository,
            RentalRepository rentalRepository,
            VehicleIncidentRepository incidentRepository,
            MaintenanceRepository maintenanceRepository,
            VehicleFuelRepository fuelRepository,
            VehicleDocumentsRepository documentsRepository,
            AuthenticationService authenticationService) {

        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.incidentRepository = incidentRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.fuelRepository = fuelRepository;
        this.documentsRepository = documentsRepository;
        this.authenticationService = authenticationService;
    }

    public List<String> getVehicleHistory(String vehicleId) {
        authenticationService.requireLogin();

        Vehicle vehicle = findVehicle(vehicleId);
        List<String> history = new ArrayList<>();

        history.add("Vehicle: " + vehicle.getId()
                + " - " + vehicle.getBrand()
                + " " + vehicle.getModel());
        history.add("Current status: " + vehicle.getStatus());

        addEnergyInformation(vehicle, history);
        addDocumentInformation(vehicle, history);
        addRentalHistory(vehicle, history);
        addIncidentHistory(vehicle, history);
        addMaintenanceHistory(vehicle, history);

        return history;
    }

    private void addEnergyInformation(Vehicle vehicle, List<String> history) {
        if (vehicle instanceof ElectricVehicle) {
            history.add("Battery level: "
                    + ((ElectricVehicle) vehicle).getBatteryLevel() + "%");
            return;
        }

        FuelRecord fuelRecord = fuelRepository.findByVehicleId(vehicle.getId())
                .orElse(null);

        if (fuelRecord != null) {
            history.add("Fuel level: " + fuelRecord.getFuelLevel() + "%");
        }
    }

    private void addDocumentInformation(Vehicle vehicle, List<String> history) {
        VehicleDocuments documents = documentsRepository
                .findByVehicleId(vehicle.getId())
                .orElse(null);

        if (documents != null) {
            history.add("Registration expiry: "
                    + documents.getRegistrationExpiryDate());
            history.add("Insurance expiry: "
                    + documents.getInsuranceExpiryDate());
        }
    }

    private void addRentalHistory(Vehicle vehicle, List<String> history) {
        for (Rental rental : rentalRepository.findAll()) {
            if (rental.getVehicle().getId().equals(vehicle.getId())) {
                history.add("Rental " + rental.getId()
                        + ": " + rental.getStartDate()
                        + " to " + rental.getEndDate()
                        + ", active=" + rental.isActive());
            }
        }
    }

    private void addIncidentHistory(Vehicle vehicle, List<String> history) {
        for (VehicleIncident incident
                : incidentRepository.findByVehicleId(vehicle.getId())) {
            history.add("Incident " + incident.getType()
                    + " on " + incident.getDate()
                    + ": " + incident.getDescription());
        }
    }

    private void addMaintenanceHistory(Vehicle vehicle, List<String> history) {
        for (MaintenanceRecord record : maintenanceRepository.findAll()) {
            if (record.getVehicleId().equals(vehicle.getId())) {
                history.add("Maintenance " + record.getStatus()
                        + ": next date " + record.getNextMaintenanceDate());
            }
        }
    }

    private Vehicle findVehicle(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle id is required.");
        }

        return vehicleRepository.findById(vehicleId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + vehicleId
                ));
    }
}
