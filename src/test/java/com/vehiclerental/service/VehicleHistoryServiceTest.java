package com.vehiclerental.service;

import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.IncidentType;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleDocuments;
import com.vehiclerental.domain.VehicleIncident;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.ManagerRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleHistoryServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldBuildCompleteHistoryForNormalVehicle() throws IOException {
        TestData data = createTestData(
                "V1,CAR,Toyota,Corolla,50,AVAILABLE"
        );
        Vehicle vehicle = data.vehicleRepository.findById("V1").get();

        data.fuelRepository.saveOrUpdate(new FuelRecord("V1", 75));
        data.documentsRepository.saveOrUpdate(new VehicleDocuments(
                "V1",
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 2, 1)
        ));
        data.rentalRepository.save(new Rental(
                "R1",
                vehicle,
                new Customer("C1", "Ahmad", "ahmad@example.com"),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 4)
        ));
        data.incidentRepository.save(new VehicleIncident(
                "I1",
                "V1",
                IncidentType.VIOLATION,
                LocalDate.of(2026, 6, 1),
                "Speeding"
        ));
        data.maintenanceRepository.save(new MaintenanceRecord(
                "M1",
                "V1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1),
                MaintenanceStatus.PENDING
        ));

        List<String> history = data.service.getVehicleHistory("V1");

        assertTrue(history.contains("Vehicle: V1 - Toyota Corolla"));
        assertTrue(history.contains("Fuel level: 75%"));
        assertTrue(history.contains("Registration expiry: 2027-01-01"));
        assertTrue(history.stream().anyMatch(line -> line.startsWith("Rental R1:")));
        assertTrue(history.stream().anyMatch(line -> line.startsWith("Incident VIOLATION")));
        assertTrue(history.stream().anyMatch(line -> line.startsWith("Maintenance PENDING")));
    }

    @Test
    void shouldShowBatteryForElectricVehicle() throws IOException {
        TestData data = createTestData(
                "E1,ELECTRIC,Tesla,Model 3,100,AVAILABLE,80"
        );

        List<String> history = data.service.getVehicleHistory("E1");

        assertTrue(history.contains("Battery level: 80%"));
    }

    @Test
    void shouldRejectBlankVehicleId() throws IOException {
        TestData data = createTestData(
                "V1,CAR,Toyota,Corolla,50,AVAILABLE"
        );

        assertThrows(IllegalArgumentException.class,
                () -> data.service.getVehicleHistory(" "));
    }

    @Test
    void shouldRejectUnknownVehicleId() throws IOException {
        TestData data = createTestData(
                "V1,CAR,Toyota,Corolla,50,AVAILABLE"
        );

        assertThrows(IllegalArgumentException.class,
                () -> data.service.getVehicleHistory("V9"));
    }

    private TestData createTestData(String vehicleLine) throws IOException {
        Path managerFile = tempDirectory.resolve("managers.txt");
        Files.write(
                managerFile,
                Arrays.asList("admin,1234"),
                StandardCharsets.UTF_8
        );
        AuthenticationService authenticationService =
                new AuthenticationService(
                        new ManagerRepository(managerFile.toString())
                );
        authenticationService.login("admin", "1234");

        Path vehicleFile = tempDirectory.resolve("vehicles.txt");
        Files.write(
                vehicleFile,
                Arrays.asList(vehicleLine),
                StandardCharsets.UTF_8
        );

        VehicleRepository vehicleRepository =
                new VehicleRepository(vehicleFile.toString());
        RentalRepository rentalRepository = new RentalRepository(
                tempDirectory.resolve("rentals.txt").toString(),
                vehicleRepository
        );
        VehicleIncidentRepository incidentRepository =
                new VehicleIncidentRepository(
                        tempDirectory.resolve("incidents.txt").toString()
                );
        MaintenanceRepository maintenanceRepository =
                new MaintenanceRepository(
                        tempDirectory.resolve("maintenance.txt").toString()
                );
        VehicleFuelRepository fuelRepository =
                new VehicleFuelRepository(
                        tempDirectory.resolve("fuel.txt").toString()
                );
        VehicleDocumentsRepository documentsRepository =
                new VehicleDocumentsRepository(
                        tempDirectory.resolve("documents.txt").toString()
                );

        VehicleHistoryService service = new VehicleHistoryService(
                vehicleRepository,
                rentalRepository,
                incidentRepository,
                maintenanceRepository,
                fuelRepository,
                documentsRepository,
                authenticationService
        );

        return new TestData(
                service,
                vehicleRepository,
                rentalRepository,
                incidentRepository,
                maintenanceRepository,
                fuelRepository,
                documentsRepository
        );
    }

    private static class TestData {
        private final VehicleHistoryService service;
        private final VehicleRepository vehicleRepository;
        private final RentalRepository rentalRepository;
        private final VehicleIncidentRepository incidentRepository;
        private final MaintenanceRepository maintenanceRepository;
        private final VehicleFuelRepository fuelRepository;
        private final VehicleDocumentsRepository documentsRepository;

        private TestData(
                VehicleHistoryService service,
                VehicleRepository vehicleRepository,
                RentalRepository rentalRepository,
                VehicleIncidentRepository incidentRepository,
                MaintenanceRepository maintenanceRepository,
                VehicleFuelRepository fuelRepository,
                VehicleDocumentsRepository documentsRepository) {
            this.service = service;
            this.vehicleRepository = vehicleRepository;
            this.rentalRepository = rentalRepository;
            this.incidentRepository = incidentRepository;
            this.maintenanceRepository = maintenanceRepository;
            this.fuelRepository = fuelRepository;
            this.documentsRepository = documentsRepository;
        }
    }
}
