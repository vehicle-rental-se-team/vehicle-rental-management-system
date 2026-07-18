package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
import com.vehiclerental.domain.VehicleDocuments;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VehicleAvailabilityServiceEdgeCaseTest {

    @Test
    void shouldRejectNullVehicle() {
        assertThrows(IllegalArgumentException.class, () ->
                service().determineStatus(null, date()));
    }

    @Test
    void shouldRejectNullDate() {
        assertThrows(IllegalArgumentException.class, () ->
                service().determineStatus(car(), null));
    }

    @Test
    void shouldReturnMaintenanceWhenMaintenanceIsDue() {
        Dependencies dependencies = dependencies();
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1", LocalDate.of(2026, 1, 1), date(), MaintenanceStatus.PENDING
        );
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record));

        assertEquals(
                VehicleStatus.MAINTENANCE,
                dependencies.service.determineStatus(car(), date())
        );
    }

    @Test
    void shouldKeepVehicleAvailableWhenMaintenanceIsLater() {
        Dependencies dependencies = dependencies();
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1", LocalDate.of(2026, 1, 1), date().plusDays(1),
                MaintenanceStatus.PENDING
        );
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record));

        assertEquals(
                VehicleStatus.AVAILABLE,
                dependencies.service.determineStatus(car(), date())
        );
    }

    @Test
    void shouldReturnAvailableForElectricVehicleAtThirtyPercent() {
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "3", 100, VehicleStatus.UNAVAILABLE, 30
        );

        assertEquals(
                VehicleStatus.AVAILABLE,
                service().determineStatus(vehicle, date())
        );
    }

    @Test
    void shouldReturnAvailableForNormalVehicleAtTwentyPercentFuel() {
        Dependencies dependencies = dependencies();
        when(dependencies.fuelRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(new FuelRecord("V1", 20)));

        assertEquals(
                VehicleStatus.AVAILABLE,
                dependencies.service.determineStatus(car(), date())
        );
    }

    @Test
    void shouldReturnAvailableWhenDocumentsAreMissing() {
        Dependencies dependencies = dependencies();
        when(dependencies.documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.empty());

        assertEquals(
                VehicleStatus.AVAILABLE,
                dependencies.service.determineStatus(car(), date())
        );
    }

    @Test
    void shouldReturnUnavailableWhenInsuranceExpired() {
        Dependencies dependencies = dependencies();
        when(dependencies.documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(new VehicleDocuments(
                        "V1", date().plusDays(1), date()
                )));

        assertEquals(
                VehicleStatus.UNAVAILABLE,
                dependencies.service.determineStatus(car(), date())
        );
    }

    private VehicleAvailabilityService service() {
        return dependencies().service;
    }

    private Dependencies dependencies() {
        return new Dependencies();
    }

    private Car car() {
        return new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
    }

    private LocalDate date() {
        return LocalDate.of(2026, 7, 10);
    }

    private static class Dependencies {
        private final RentalRepository rentalRepository = mock(RentalRepository.class);
        private final MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        private final VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        private final VehicleFuelRepository fuelRepository = mock(VehicleFuelRepository.class);
        private final VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        private final VehicleAvailabilityService service = new VehicleAvailabilityService(
                rentalRepository,
                maintenanceRepository,
                incidentRepository,
                fuelRepository,
                documentsRepository
        );
    }
}
