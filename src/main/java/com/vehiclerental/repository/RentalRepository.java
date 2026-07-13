package com.vehiclerental.repository;

import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;

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

public class RentalRepository {

    private static final String RENTALS_FILE_PATH = "data/rentals.txt";

    private final List<Rental> rentals;
    private final VehicleRepository vehicleRepository;

    public RentalRepository(VehicleRepository vehicleRepository) {
        if (vehicleRepository == null) {
            throw new IllegalArgumentException("Vehicle repository is required.");
        }

        this.vehicleRepository = vehicleRepository;
        this.rentals = new ArrayList<>();
        createRentalsFileIfMissing();
        loadRentalsFromFile();
    }

    public void save(Rental rental) {
        if (rental == null) {
            throw new IllegalArgumentException("Rental is required.");
        }

        rentals.add(rental);
        saveAllRentalsToFile();
    }

    public void update(Rental rental) {
        if (rental == null) {
            throw new IllegalArgumentException("Rental is required.");
        }

        saveAllRentalsToFile();
    }

    public Optional<Rental> findById(String id) {
        for (Rental rental : rentals) {
            if (rental.getId().equals(id)) {
                return Optional.of(rental);
            }
        }

        return Optional.empty();
    }

    public Optional<Rental> findActiveRentalByVehicleId(String vehicleId) {
        for (Rental rental : rentals) {
            if (rental.isActive() && rental.getVehicle().getId().equals(vehicleId)) {
                return Optional.of(rental);
            }
        }

        return Optional.empty();
    }

    public List<Rental> findAll() {
        return new ArrayList<>(rentals);
    }

    private void createRentalsFileIfMissing() {
        File rentalsFile = new File(RENTALS_FILE_PATH);
        File parentDirectory = rentalsFile.getParentFile();

        try {
            if (parentDirectory != null && !parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }

            if (!rentalsFile.exists()) {
                rentalsFile.createNewFile();
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not create rentals file: " + RENTALS_FILE_PATH,
                    exception
            );
        }
    }

    private void loadRentalsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(RENTALS_FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Rental rental = convertLineToRental(line);
                    rentals.add(rental);

                    if (rental.isActive()) {
                        rental.getVehicle().setStatus(VehicleStatus.RENTED);
                    }
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not load rentals from file: " + RENTALS_FILE_PATH,
                    exception
            );
        }
    }

    private Rental convertLineToRental(String line) {
        String[] parts = line.split(",", -1);

        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid rental data: " + line);
        }

        String rentalId = parts[0].trim();
        String vehicleId = parts[1].trim();
        String customerId = parts[2].trim();
        String customerName = parts[3].trim();
        String customerEmail = parts[4].trim();
        LocalDate startDate = LocalDate.parse(parts[5].trim());
        LocalDate endDate = LocalDate.parse(parts[6].trim());
        boolean active = Boolean.parseBoolean(parts[7].trim());

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found for rental: " + vehicleId
                ));

        Customer customer = new Customer(customerId, customerName, customerEmail);

        return new Rental(
                rentalId,
                vehicle,
                customer,
                startDate,
                endDate,
                active
        );
    }

    private void saveAllRentalsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RENTALS_FILE_PATH))) {
            for (Rental rental : rentals) {
                writer.write(convertRentalToLine(rental));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not save rentals to file: " + RENTALS_FILE_PATH,
                    exception
            );
        }
    }

    private String convertRentalToLine(Rental rental) {
        return rental.getId() + "," +
                rental.getVehicle().getId() + "," +
                rental.getCustomer().getId() + "," +
                cleanText(rental.getCustomerName()) + "," +
                cleanText(rental.getCustomerEmail()) + "," +
                rental.getStartDate() + "," +
                rental.getEndDate() + "," +
                rental.isActive();
    }

    private String cleanText(String value) {
        return value.replace(",", " ").trim();
    }
}
