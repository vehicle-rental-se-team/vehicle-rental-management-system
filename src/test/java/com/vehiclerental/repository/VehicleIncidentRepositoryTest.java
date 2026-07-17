package com.vehiclerental.repository;

import com.vehiclerental.domain.IncidentType;
import com.vehiclerental.domain.VehicleIncident;
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

class VehicleIncidentRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldCreateIncidentFileWhenMissing() {
        Path file = tempDirectory.resolve("incidents/incidents.txt");

        new VehicleIncidentRepository(file.toString());

        assertTrue(Files.exists(file));
    }

    @Test
    void shouldSaveAndReloadIncident() {
        Path file = tempDirectory.resolve("incidents.txt");
        VehicleIncidentRepository repository =
                new VehicleIncidentRepository(file.toString());
        VehicleIncident incident = new VehicleIncident(
                "I1",
                "V1",
                IncidentType.ACCIDENT,
                LocalDate.of(2026, 7, 1),
                "Front, damage"
        );

        repository.save(incident);

        VehicleIncidentRepository loadedRepository =
                new VehicleIncidentRepository(file.toString());
        assertTrue(loadedRepository.findById("I1").isPresent());
        assertEquals("Front  damage",
                loadedRepository.findById("I1").get().getDescription());
        assertTrue(loadedRepository.hasPendingAccident("V1"));
    }

    @Test
    void shouldSaveCompletedInspectionAfterUpdate() {
        Path file = tempDirectory.resolve("incidents.txt");
        VehicleIncidentRepository repository =
                new VehicleIncidentRepository(file.toString());
        VehicleIncident incident = new VehicleIncident(
                "I1",
                "V1",
                IncidentType.ACCIDENT,
                LocalDate.of(2026, 7, 1),
                "Front damage"
        );
        repository.save(incident);

        incident.completeInspection();
        repository.update(incident);

        VehicleIncidentRepository loadedRepository =
                new VehicleIncidentRepository(file.toString());
        assertFalse(loadedRepository.hasPendingAccident("V1"));
        assertTrue(loadedRepository.findById("I1").get()
                .isInspectionCompleted());
    }

    @Test
    void shouldLoadOldIncidentLineWithoutInspectionValue() throws IOException {
        Path file = tempDirectory.resolve("incidents.txt");
        Files.write(
                file,
                Arrays.asList(
                        "I1,V1,VIOLATION,2026-07-01,Speeding",
                        "I2,V1,ACCIDENT,2026-07-02,Damage"
                ),
                StandardCharsets.UTF_8
        );

        VehicleIncidentRepository repository =
                new VehicleIncidentRepository(file.toString());

        assertTrue(repository.findById("I1").get().isInspectionCompleted());
        assertFalse(repository.findById("I2").get().isInspectionCompleted());
        assertEquals(2, repository.findByVehicleId("V1").size());
        assertEquals(1,
                repository.findPendingAccidentsByVehicleId("V1").size());
    }

    @Test
    void shouldReturnEmptyResultsForBlankIds() {
        VehicleIncidentRepository repository =
                new VehicleIncidentRepository(
                        tempDirectory.resolve("incidents.txt").toString()
                );

        assertFalse(repository.findById(null).isPresent());
        assertFalse(repository.findById(" ").isPresent());
        assertTrue(repository.findByVehicleId(null).isEmpty());
        assertTrue(repository.findByVehicleId(" ").isEmpty());
    }

    @Test
    void shouldRejectNullIncident() {
        VehicleIncidentRepository repository =
                new VehicleIncidentRepository(
                        tempDirectory.resolve("incidents.txt").toString()
                );

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.update(null));
    }

    @Test
    void shouldRejectUpdateForMissingIncident() {
        VehicleIncidentRepository repository =
                new VehicleIncidentRepository(
                        tempDirectory.resolve("incidents.txt").toString()
                );
        VehicleIncident incident = new VehicleIncident(
                "I9",
                "V1",
                IncidentType.VIOLATION,
                LocalDate.of(2026, 7, 1),
                "Speeding"
        );

        assertThrows(IllegalArgumentException.class,
                () -> repository.update(incident));
    }

    @Test
    void shouldRejectInvalidIncidentData() throws IOException {
        Path file = tempDirectory.resolve("incidents.txt");
        Files.write(
                file,
                Arrays.asList("invalid,incident"),
                StandardCharsets.UTF_8
        );

        assertThrows(IllegalArgumentException.class,
                () -> new VehicleIncidentRepository(file.toString()));
    }
}
