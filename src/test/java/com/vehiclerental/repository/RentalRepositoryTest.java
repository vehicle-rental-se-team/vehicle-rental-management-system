package com.vehiclerental.repository;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RentalRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldCreateRentalFileWhenMissing() throws IOException {
        VehicleRepository vehicleRepository = createVehicleRepository();
        Path rentalFile = tempDirectory.resolve("new-folder/rentals.txt");

        new RentalRepository(rentalFile.toString(), vehicleRepository);

        assertTrue(Files.exists(rentalFile));
    }

    @Test
    void shouldSaveAndReloadRental() throws IOException {
        VehicleRepository vehicleRepository = createVehicleRepository();
        Path rentalFile = tempDirectory.resolve("rentals.txt");
        RentalRepository repository = new RentalRepository(
                rentalFile.toString(), vehicleRepository
        );

        repository.save(createRental(vehicleRepository.findById("V1").get()));

        RentalRepository loadedRepository = new RentalRepository(
                rentalFile.toString(), vehicleRepository
        );

        assertTrue(loadedRepository.findById("R1").isPresent());
        assertEquals("Ahmad Ali",
                loadedRepository.findById("R1").get().getCustomerName());
        assertTrue(loadedRepository.findActiveRentalByVehicleId("V1").isPresent());
    }

    @Test
    void shouldCleanCommasBeforeSavingCustomerData() throws IOException {
        VehicleRepository vehicleRepository = createVehicleRepository();
        Path rentalFile = tempDirectory.resolve("rentals.txt");
        RentalRepository repository = new RentalRepository(
                rentalFile.toString(), vehicleRepository
        );
        Customer customer = new Customer(
                "C1", "Ahmad, Ali", "ahmad,test@example.com"
        );
        Rental rental = new Rental(
                "R1",
                vehicleRepository.findById("V1").get(),
                customer,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 4)
        );

        repository.save(rental);

        String savedLine = new String(
                Files.readAllBytes(rentalFile),
                StandardCharsets.UTF_8
        );
        assertTrue(savedLine.contains("Ahmad  Ali"));
        assertTrue(savedLine.contains("ahmad test@example.com"));
    }

    @Test
    void shouldSaveClosedRentalAfterUpdate() throws IOException {
        VehicleRepository vehicleRepository = createVehicleRepository();
        Path rentalFile = tempDirectory.resolve("rentals.txt");
        RentalRepository repository = new RentalRepository(
                rentalFile.toString(), vehicleRepository
        );
        Rental rental = createRental(vehicleRepository.findById("V1").get());
        repository.save(rental);

        rental.close();
        repository.update(rental);

        RentalRepository loadedRepository = new RentalRepository(
                rentalFile.toString(), vehicleRepository
        );
        assertFalse(loadedRepository.findById("R1").get().isActive());
        assertFalse(loadedRepository.findActiveRentalByVehicleId("V1").isPresent());
    }

    @Test
    void shouldMarkVehicleAsRentedWhenActiveRentalIsLoaded() throws IOException {
        VehicleRepository vehicleRepository = createVehicleRepository();
        Path rentalFile = tempDirectory.resolve("rentals.txt");
        Files.write(
                rentalFile,
                Arrays.asList(
                        "R1,V1,C1,Ahmad,ahmad@example.com,2026-07-01,2026-07-04,true"
                ),
                StandardCharsets.UTF_8
        );

        new RentalRepository(rentalFile.toString(), vehicleRepository);

        assertEquals(VehicleStatus.RENTED,
                vehicleRepository.findById("V1").get().getStatus());
    }

    @Test
    void shouldRejectNullRental() throws IOException {
        RentalRepository repository = new RentalRepository(
                tempDirectory.resolve("rentals.txt").toString(),
                createVehicleRepository()
        );

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.update(null));
    }

    @Test
    void shouldRejectNullVehicleRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new RentalRepository(
                        tempDirectory.resolve("rentals.txt").toString(),
                        null
                ));
    }

    @Test
    void shouldRejectInvalidRentalData() throws IOException {
        VehicleRepository vehicleRepository = createVehicleRepository();
        Path rentalFile = tempDirectory.resolve("rentals.txt");
        Files.write(
                rentalFile,
                Arrays.asList("invalid,rental"),
                StandardCharsets.UTF_8
        );

        assertThrows(IllegalArgumentException.class, () ->
                new RentalRepository(rentalFile.toString(), vehicleRepository));
    }

    @Test
    void shouldRejectRentalForUnknownVehicle() throws IOException {
        VehicleRepository vehicleRepository = createVehicleRepository();
        Path rentalFile = tempDirectory.resolve("rentals.txt");
        Files.write(
                rentalFile,
                Arrays.asList(
                        "R1,V9,C1,Ahmad,ahmad@example.com,2026-07-01,2026-07-04,true"
                ),
                StandardCharsets.UTF_8
        );

        assertThrows(IllegalArgumentException.class, () ->
                new RentalRepository(rentalFile.toString(), vehicleRepository));
    }

    private VehicleRepository createVehicleRepository() throws IOException {
        Path vehicleFile = tempDirectory.resolve("vehicles.txt");
        Files.write(
                vehicleFile,
                Arrays.asList("V1,CAR,Toyota,Corolla,50,AVAILABLE"),
                StandardCharsets.UTF_8
        );
        return new VehicleRepository(vehicleFile.toString());
    }

    private Rental createRental(Vehicle vehicle) {
        Customer customer = new Customer(
                "C1", "Ahmad Ali", "ahmad@example.com"
        );
        return new Rental(
                "R1",
                vehicle,
                customer,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 4)
        );
    }
}
