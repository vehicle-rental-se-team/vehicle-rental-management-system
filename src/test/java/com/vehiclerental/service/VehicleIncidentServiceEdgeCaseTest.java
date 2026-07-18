package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.IncidentType;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleIncident;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleIncidentServiceEdgeCaseTest {

    @Test
    void shouldRejectNullVehicleRepository() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new VehicleIncidentService(
                        null,
                        dependencies.incidentRepository,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        "manager@test.com"
                ));
    }

    @Test
    void shouldRejectNullIncidentRepository() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new VehicleIncidentService(
                        dependencies.vehicleRepository,
                        null,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        "manager@test.com"
                ));
    }

    @Test
    void shouldRejectBlankNotificationRecipient() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new VehicleIncidentService(
                        dependencies.vehicleRepository,
                        dependencies.incidentRepository,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        " "
                ));
    }

    @Test
    void shouldRejectBlankVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.recordIncident(
                        " ", IncidentType.ACCIDENT, date(), "Damage"
                ));
    }

    @Test
    void shouldRejectNullIncidentType() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.recordIncident("V1", null, date(), "Damage"));
    }

    @Test
    void shouldRejectNullIncidentDate() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.recordIncident(
                        "V1", IncidentType.ACCIDENT, null, "Damage"
                ));
    }

    @Test
    void shouldRejectUnknownVehicle() {
        Dependencies dependencies = dependencies();
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                dependencies.service.recordIncident(
                        "V1", IncidentType.VIOLATION, date(), "Speeding"
                ));
    }

    @Test
    void shouldRejectNullInspectionDate() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.completeInspection("V1", null));
    }

    @Test
    void shouldUseAvailabilityServiceAfterInspection() {
        Dependencies dependencies = readyForInspection(car(), true);

        int completed = dependencies.service.completeInspection("V1", date());

        assertEquals(1, completed);
        verify(dependencies.availabilityService).applyStatus(
                dependencies.vehicle, date()
        );
    }

    @Test
    void shouldKeepRentedStatusAfterInspection() {
        Dependencies dependencies = readyForInspection(car(), false);
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.of(mock(Rental.class)));

        dependencies.service.completeInspection("V1", date());

        assertEquals(VehicleStatus.RENTED, dependencies.vehicle.getStatus());
    }

    @Test
    void shouldKeepMaintenanceWhenMaintenanceIsDue() {
        Dependencies dependencies = readyForInspection(car(), false);
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1", date().minusMonths(6), date(), MaintenanceStatus.PENDING
        );
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record));

        dependencies.service.completeInspection("V1", date());

        assertEquals(VehicleStatus.MAINTENANCE, dependencies.vehicle.getStatus());
    }

    @Test
    void shouldMakeLowBatteryVehicleUnavailableAfterInspection() {
        ElectricVehicle electricVehicle = new ElectricVehicle(
                "V1", "Tesla", "3", 100, VehicleStatus.MAINTENANCE, 20
        );
        Dependencies dependencies = readyForInspection(electricVehicle, false);
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.empty());

        dependencies.service.completeInspection("V1", date());

        assertEquals(VehicleStatus.UNAVAILABLE, electricVehicle.getStatus());
    }

    @Test
    void shouldReturnPendingAccidents() {
        Dependencies dependencies = dependencies();
        when(dependencies.incidentRepository.findPendingAccidentsByVehicleId("V1"))
                .thenReturn(Collections.singletonList(accident()));

        assertEquals(1, dependencies.service.getPendingAccidents("V1").size());
    }

    @Test
    void shouldReturnVehicleIncidents() {
        Dependencies dependencies = dependencies();
        when(dependencies.incidentRepository.findByVehicleId("V1"))
                .thenReturn(Collections.singletonList(accident()));

        assertEquals(1, dependencies.service.getVehicleIncidents("V1").size());
    }

    @Test
    void shouldReturnAllIncidents() {
        Dependencies dependencies = dependencies();
        when(dependencies.incidentRepository.findAll())
                .thenReturn(Collections.singletonList(accident()));

        assertEquals(1, dependencies.service.getAllIncidents().size());
    }

    private Dependencies readyForInspection(Vehicle vehicle, boolean useAvailability) {
        Dependencies dependencies = new Dependencies(useAvailability);
        dependencies.vehicle = vehicle;
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(vehicle));
        when(dependencies.incidentRepository.findPendingAccidentsByVehicleId("V1"))
                .thenReturn(Collections.singletonList(accident()));
        return dependencies;
    }

    private Dependencies dependencies() {
        return new Dependencies(false);
    }

    private VehicleIncident accident() {
        return new VehicleIncident(
                "I1", "V1", IncidentType.ACCIDENT, date(), "Damage"
        );
    }

    private Car car() {
        return new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.MAINTENANCE);
    }

    private LocalDate date() {
        return LocalDate.of(2026, 7, 10);
    }

    private static class Dependencies {
        private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        private final VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        private final NotificationPublisher publisher = mock(NotificationPublisher.class);
        private final AuthenticationService authenticationService = mock(AuthenticationService.class);
        private final RentalRepository rentalRepository = mock(RentalRepository.class);
        private final MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        private final VehicleAvailabilityService availabilityService;
        private final VehicleIncidentService service;
        private Vehicle vehicle;

        private Dependencies(boolean useAvailabilityService) {
            availabilityService = useAvailabilityService
                    ? mock(VehicleAvailabilityService.class)
                    : null;
            service = new VehicleIncidentService(
                    vehicleRepository,
                    incidentRepository,
                    publisher,
                    authenticationService,
                    rentalRepository,
                    maintenanceRepository,
                    "manager@test.com",
                    availabilityService
            );
        }
    }
}
