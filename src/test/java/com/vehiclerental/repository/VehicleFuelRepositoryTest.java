package com.vehiclerental.repository;

import com.vehiclerental.domain.FuelRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleFuelRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldCreateFuelFileWhenMissing() {
        Path file = tempDirectory.resolve("fuel/fuel.txt");

        new VehicleFuelRepository(file.toString());

        assertTrue(Files.exists(file));
    }

    @Test
    void shouldSaveAndReloadFuelRecord() {
        Path file = tempDirectory.resolve("fuel.txt");
        VehicleFuelRepository repository =
                new VehicleFuelRepository(file.toString());

        repository.saveOrUpdate(new FuelRecord("V1", 80));

        VehicleFuelRepository loadedRepository =
                new VehicleFuelRepository(file.toString());
        assertTrue(loadedRepository.findByVehicleId("V1").isPresent());
        assertEquals(80,
                loadedRepository.findByVehicleId("V1").get().getFuelLevel());
    }

    @Test
    void shouldUpdateExistingFuelRecord() {
        Path file = tempDirectory.resolve("fuel.txt");
        VehicleFuelRepository repository =
                new VehicleFuelRepository(file.toString());
        repository.saveOrUpdate(new FuelRecord("V1", 80));

        repository.saveOrUpdate(new FuelRecord("V1", 25));

        assertEquals(1, repository.findAll().size());
        assertEquals(25,
                repository.findByVehicleId("V1").get().getFuelLevel());
    }

    @Test
    void shouldReturnEmptyForUnknownVehicle() {
        VehicleFuelRepository repository = new VehicleFuelRepository(
                tempDirectory.resolve("fuel.txt").toString()
        );

        assertFalse(repository.findByVehicleId("V9").isPresent());
    }

    @Test
    void shouldRejectNullFuelRecord() {
        VehicleFuelRepository repository = new VehicleFuelRepository(
                tempDirectory.resolve("fuel.txt").toString()
        );

        assertThrows(IllegalArgumentException.class,
                () -> repository.saveOrUpdate(null));
    }

    @Test
    void shouldRejectInvalidFuelData() throws IOException {
        Path file = tempDirectory.resolve("fuel.txt");
        Files.write(
                file,
                Arrays.asList("invalid,fuel,data"),
                StandardCharsets.UTF_8
        );

        assertThrows(IllegalArgumentException.class,
                () -> new VehicleFuelRepository(file.toString()));
    }
}
