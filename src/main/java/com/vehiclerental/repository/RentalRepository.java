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

/**
 * Stores rental data in memory and in a text file.
 */
public class RentalRepository {

    /** Default path of the rentals data file. */
    private static final String RENTALS_FILE_PATH = "data/rentals.txt";

    /** Path of the file used by this repository. */
    private final String filePath;
    /** List of rentals currently loaded in memory. */
    private final List<Rental> rentals;
    /** Repository used to connect rental records with vehicles. */
    private final VehicleRepository vehicleRepository;

    /**
     * Creates the repository using the default rentals file.
     *
     * @param vehicleRepository the vehicle repository
     */
    public RentalRepository(VehicleRepository vehicleRepository) {
        this(RENTALS_FILE_PATH, vehicleRepository);
    }

    /**
     * Creates the repository using a selected file path.
     *
     * @param filePath the rentals file path
     * @param vehicleRepository the vehicle repository
     * @throws IllegalArgumentException when the vehicle repository is missing
     */
    public RentalRepository(
            String filePath,
            VehicleRepository vehicleRepository) {

        if (vehicleRepository == null) {
            throw new IllegalArgumentException("Vehicle repository is required.");
        }

        this.filePath = filePath;
        this.vehicleRepository = vehicleRepository;
        this.rentals = new ArrayList<>();
        createRentalsFileIfMissing();
        loadRentalsFromFile();
    }

    /**
     * Adds a rental and saves all rentals to the file.
     *
     * @param rental the rental to save
     */
    public void save(Rental rental) {
        if (rental == null) {
            throw new IllegalArgumentException("Rental is required.");
        }

        rentals.add(rental);
        saveAllRentalsToFile();
    }

    /**
     * Saves the current rental list after a rental is changed.
     *
     * @param rental the changed rental
     */
    public void update(Rental rental) {
        if (rental == null) {
            throw new IllegalArgumentException("Rental is required.");
        }

        saveAllRentalsToFile();
    }

    /**
     * Searches for a rental by id.
     *
     * @param id the rental id
     * @return the rental when found
     */
    public Optional<Rental> findById(String id) {
        for (Rental rental : rentals) {
            if (rental.getId().equals(id)) {
                return Optional.of(rental);
            }
        }

        return Optional.empty();
    }

    /**
     * Searches for an active rental for a vehicle.
     *
     * @param vehicleId the vehicle id
     * @return the active rental when found
     */
    public Optional<Rental> findActiveRentalByVehicleId(String vehicleId) {
        for (Rental rental : rentals) {
            if (rental.isActive()
                    && rental.getVehicle().getId().equals(vehicleId)) {
                return Optional.of(rental);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns a copy of all rentals.
     *
     * @return all rentals
     */
    public List<Rental> findAll() {
        return new ArrayList<>(rentals);
    }

    /**
     * Creates the rentals file and its parent folder when needed.
     */
    private void createRentalsFileIfMissing() {
        File rentalsFile = new File(filePath);
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
                    "Could not create rentals file: " + filePath,
                    exception
            );
        }
    }

    /**
     * Loads rental records from the text file.
     */
    private void loadRentalsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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
                    "Could not load rentals from file: " + filePath,
                    exception
            );
        }
    }

    /**
     * Converts one file line into a rental object.
     *
     * @param line one rental line from the file
     * @return the converted rental
     */
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

        Customer customer = new Customer(
                customerId,
                customerName,
                customerEmail
        );

        return new Rental(
                rentalId,
                vehicle,
                customer,
                startDate,
                endDate,
                active
        );
    }

    /**
     * Writes all rentals to the text file.
     */
    private void saveAllRentalsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Rental rental : rentals) {
                writer.write(convertRentalToLine(rental));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not save rentals to file: " + filePath,
                    exception
            );
        }
    }

    /**
     * Converts a rental into one line for the data file.
     *
     * @param rental the rental to convert
     * @return the rental as one text line
     */
    private String convertRentalToLine(Rental rental) {
        return rental.getId() + ","
                + rental.getVehicle().getId() + ","
                + rental.getCustomer().getId() + ","
                + cleanText(rental.getCustomerName()) + ","
                + cleanText(rental.getCustomerEmail()) + ","
                + rental.getStartDate() + ","
                + rental.getEndDate() + ","
                + rental.isActive();
    }

    /**
     * Removes commas from values before writing them to the file.
     *
     * @param value the text to clean
     * @return the cleaned text
     */
    private String cleanText(String value) {
        return value.replace(",", " ").trim();
    }
}
