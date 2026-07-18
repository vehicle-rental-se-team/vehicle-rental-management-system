package com.vehiclerental.repository;

import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
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

class MaintenanceRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldCreateMaintenanceFileWhenMissing() {
        Path file = tempDirectory.resolve("records/maintenance.txt");

        new MaintenanceRepository(file.toString());

        assertTrue(Files.exists(file));
    }

    @Test
    void shouldSaveAndReloadPendingMaintenance() {
        Path file = tempDirectory.resolve("maintenance.txt");
        MaintenanceRepository repository =
                new MaintenanceRepository(file.toString());

        repository.save(createRecord());

        MaintenanceRepository loadedRepository =
                new MaintenanceRepository(file.toString());
        assertTrue(loadedRepository.findPendingByVehicleId("V1").isPresent());
        assertEquals(1, loadedRepository.findAll().size());
    }

    @Test
    void shouldSaveCompletedMaintenanceAfterUpdate() {
        Path file = tempDirectory.resolve("maintenance.txt");
        MaintenanceRepository repository =
                new MaintenanceRepository(file.toString());
        MaintenanceRecord record = createRecord();
        repository.save(record);

        record.complete();
        repository.update(record);

        MaintenanceRepository loadedRepository =
                new MaintenanceRepository(file.toString());
        assertFalse(loadedRepository.findPendingByVehicleId("V1").isPresent());
        assertEquals(MaintenanceStatus.COMPLETED,
                loadedRepository.findAll().get(0).getStatus());
    }

    @Test
    void shouldReturnEmptyForBlankVehicleId() {
        MaintenanceRepository repository = new MaintenanceRepository(
                tempDirectory.resolve("maintenance.txt").toString()
        );

        assertFalse(repository.findPendingByVehicleId(null).isPresent());
        assertFalse(repository.findPendingByVehicleId("  ").isPresent());
    }

    @Test
    void shouldRejectNullMaintenanceRecord() {
        MaintenanceRepository repository = new MaintenanceRepository(
                tempDirectory.resolve("maintenance.txt").toString()
        );

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.update(null));
    }

    @Test
    void shouldRejectInvalidMaintenanceData() throws IOException {
        Path file = tempDirectory.resolve("maintenance.txt");
        Files.write(
                file,
                Arrays.asList("invalid,maintenance"),
                StandardCharsets.UTF_8
        );

        assertThrows(IllegalArgumentException.class,
                () -> new MaintenanceRepository(file.toString()));
    }

    private MaintenanceRecord createRecord() {
        return new MaintenanceRecord(
                "M1",
                "V1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1),
                MaintenanceStatus.PENDING
        );
    }
}
