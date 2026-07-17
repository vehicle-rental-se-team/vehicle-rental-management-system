package com.vehiclerental.repository;

import com.vehiclerental.domain.VehicleDocuments;
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

class VehicleDocumentsRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldCreateDocumentsFileWhenMissing() {
        Path file = tempDirectory.resolve("documents/vehicle_documents.txt");

        new VehicleDocumentsRepository(file.toString());

        assertTrue(Files.exists(file));
    }

    @Test
    void shouldSaveAndReloadDocuments() {
        Path file = tempDirectory.resolve("vehicle_documents.txt");
        VehicleDocumentsRepository repository =
                new VehicleDocumentsRepository(file.toString());

        repository.saveOrUpdate(new VehicleDocuments(
                "V1",
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 2, 1)
        ));

        VehicleDocumentsRepository loadedRepository =
                new VehicleDocumentsRepository(file.toString());
        assertTrue(loadedRepository.findByVehicleId("V1").isPresent());
        assertEquals(LocalDate.of(2027, 2, 1),
                loadedRepository.findByVehicleId("V1").get()
                        .getInsuranceExpiryDate());
    }

    @Test
    void shouldUpdateExistingDocuments() {
        Path file = tempDirectory.resolve("vehicle_documents.txt");
        VehicleDocumentsRepository repository =
                new VehicleDocumentsRepository(file.toString());
        repository.saveOrUpdate(new VehicleDocuments(
                "V1",
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 2, 1)
        ));

        repository.saveOrUpdate(new VehicleDocuments(
                "V1",
                LocalDate.of(2028, 1, 1),
                LocalDate.of(2028, 2, 1)
        ));

        assertEquals(1, repository.findAll().size());
        assertEquals(LocalDate.of(2028, 1, 1),
                repository.findByVehicleId("V1").get()
                        .getRegistrationExpiryDate());
    }

    @Test
    void shouldReturnEmptyForUnknownVehicle() {
        VehicleDocumentsRepository repository =
                new VehicleDocumentsRepository(
                        tempDirectory.resolve("vehicle_documents.txt").toString()
                );

        assertFalse(repository.findByVehicleId("V9").isPresent());
    }

    @Test
    void shouldRejectNullDocuments() {
        VehicleDocumentsRepository repository =
                new VehicleDocumentsRepository(
                        tempDirectory.resolve("vehicle_documents.txt").toString()
                );

        assertThrows(IllegalArgumentException.class,
                () -> repository.saveOrUpdate(null));
    }

    @Test
    void shouldRejectInvalidDocumentsData() throws IOException {
        Path file = tempDirectory.resolve("vehicle_documents.txt");
        Files.write(
                file,
                Arrays.asList("invalid,documents"),
                StandardCharsets.UTF_8
        );

        assertThrows(IllegalArgumentException.class,
                () -> new VehicleDocumentsRepository(file.toString()));
    }
}
