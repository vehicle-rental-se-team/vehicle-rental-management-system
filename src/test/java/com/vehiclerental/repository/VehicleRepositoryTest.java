package com.vehiclerental.repository;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldLoadAllVehicleTypes() throws IOException {
        Path file = createVehicleFile(
                "C1,CAR,Toyota,Corolla,50,AVAILABLE",
                "M1,MOTORCYCLE,Honda,CBR,30,AVAILABLE",
                "V1,VAN,Ford,Transit,70,AVAILABLE",
                "T1,TRUCK,Volvo,FH,120,MAINTENANCE",
                "E1,ELECTRIC,Tesla,Model 3,100,AVAILABLE,80",
                "E2,ELECTRIC_VEHICLE,Nissan,Leaf,90,UNAVAILABLE,20"
        );

        VehicleRepository repository = new VehicleRepository(file.toString());
        List<Vehicle> vehicles = repository.findAll();

        assertEquals(6, vehicles.size());
        assertEquals("CAR", repository.findById("C1").get().getType());
        assertEquals("MOTORCYCLE", repository.findById("M1").get().getType());
        assertEquals("VAN", repository.findById("V1").get().getType());
        assertEquals("TRUCK", repository.findById("T1").get().getType());
        assertTrue(repository.findById("E1").get() instanceof ElectricVehicle);
        assertEquals(80, ((ElectricVehicle) repository.findById("E1").get())
                .getBatteryLevel());
    }

    @Test
    void shouldSaveAndReloadVehicle() throws IOException {
        Path file = createVehicleFile();
        VehicleRepository repository = new VehicleRepository(file.toString());

        repository.addVehicle(new Car(
                "C1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE
        ));

        VehicleRepository loadedRepository =
                new VehicleRepository(file.toString());

        assertTrue(loadedRepository.findById("C1").isPresent());
        assertEquals("Toyota",
                loadedRepository.findById("C1").get().getBrand());
    }

    @Test
    void shouldUpdateVehicleStatusInFile() throws IOException {
        Path file = createVehicleFile(
                "C1,CAR,Toyota,Corolla,50,AVAILABLE"
        );
        VehicleRepository repository = new VehicleRepository(file.toString());
        Vehicle vehicle = repository.findById("C1").get();

        vehicle.setStatus(VehicleStatus.RENTED);
        repository.updateVehicle(vehicle);

        VehicleRepository loadedRepository =
                new VehicleRepository(file.toString());
        assertEquals(VehicleStatus.RENTED,
                loadedRepository.findById("C1").get().getStatus());
    }

    @Test
    void shouldRejectDuplicateVehicleId() throws IOException {
        Path file = createVehicleFile(
                "C1,CAR,Toyota,Corolla,50,AVAILABLE"
        );
        VehicleRepository repository = new VehicleRepository(file.toString());

        assertThrows(IllegalArgumentException.class, () ->
                repository.addVehicle(new Car(
                        "C1", "Honda", "Civic", 55,
                        VehicleStatus.AVAILABLE
                )));
    }

    @Test
    void shouldRejectNullVehicle() throws IOException {
        VehicleRepository repository =
                new VehicleRepository(createVehicleFile().toString());

        assertThrows(IllegalArgumentException.class,
                () -> repository.addVehicle(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.updateVehicle(null));
    }

    @Test
    void shouldRejectUpdateForMissingVehicle() throws IOException {
        VehicleRepository repository =
                new VehicleRepository(createVehicleFile().toString());
        Car vehicle = new Car(
                "C1", "Toyota", "Corolla", 50,
                VehicleStatus.AVAILABLE
        );

        assertThrows(IllegalArgumentException.class,
                () -> repository.updateVehicle(vehicle));
        assertFalse(repository.findById("C1").isPresent());
    }

    @Test
    void shouldRejectElectricVehicleWithoutBatteryLevel() throws IOException {
        Path file = createVehicleFile(
                "E1,ELECTRIC,Tesla,Model 3,100,AVAILABLE"
        );

        assertThrows(IllegalArgumentException.class,
                () -> new VehicleRepository(file.toString()));
    }

    @Test
    void shouldRejectUnknownVehicleType() throws IOException {
        Path file = createVehicleFile(
                "X1,BOAT,Brand,Model,50,AVAILABLE"
        );

        assertThrows(IllegalArgumentException.class,
                () -> new VehicleRepository(file.toString()));
    }

    @Test
    void shouldRejectInvalidVehicleLine() throws IOException {
        Path file = createVehicleFile("invalid,vehicle");

        assertThrows(IllegalArgumentException.class,
                () -> new VehicleRepository(file.toString()));
    }

    private Path createVehicleFile(String... lines) throws IOException {
        Path file = tempDirectory.resolve("vehicles.txt");
        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        return file;
    }
}
