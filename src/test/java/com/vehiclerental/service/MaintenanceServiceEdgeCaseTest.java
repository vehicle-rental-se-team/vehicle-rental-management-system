package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaintenanceServiceEdgeCaseTest {

    @Test
    void shouldRejectMissingDependency() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceService(
                        null,
                        dependencies.rentalRepository,
                        dependencies.maintenanceRepository,
                        dependencies.incidentRepository,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        "manager@test.com",
                        dependencies.availabilityService
                ));
    }

    @Test
    void shouldRejectBlankNotificationRecipient() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new MaintenanceService(
                        dependencies.vehicleRepository,
                        dependencies.rentalRepository,
                        dependencies.maintenanceRepository,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        " "
                ));
    }

    @Test
    void shouldRejectBlankVehicleIdWhenScheduling() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.scheduleMaintenance(" ", date(1)));
    }

    @Test
    void shouldRejectNullLastMaintenanceDate() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.scheduleMaintenance("V1", null));
    }

    @Test
    void shouldRejectUnknownVehicleWhenScheduling() {
        Dependencies dependencies = dependencies();
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                dependencies.service.scheduleMaintenance("V1", date(1)));
    }

    @Test
    void shouldIgnoreCompletedMaintenanceRecord() {
        Dependencies dependencies = dependencies();
        MaintenanceRecord record = record(MaintenanceStatus.COMPLETED);
        when(dependencies.maintenanceRepository.findAll())
                .thenReturn(Collections.singletonList(record));

        assertEquals(0, dependencies.service.checkMaintenance(date(10)));
        verify(dependencies.publisher, never()).notifyObservers(
                eq("manager@test.com"), contains("maintenance")
        );
    }

    @Test
    void shouldSendOverdueMaintenanceNotification() {
        Dependencies dependencies = dependencies();
        Car car = car();
        when(dependencies.maintenanceRepository.findAll())
                .thenReturn(Collections.singletonList(record(MaintenanceStatus.PENDING)));
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car));
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());

        int count = dependencies.service.checkMaintenance(date(11));

        assertEquals(1, count);
        verify(dependencies.publisher).notifyObservers(
                eq("manager@test.com"), contains("overdue maintenance")
        );
    }

    @Test
    void shouldNotChangeStatusWhenVehicleIsRented() {
        Dependencies dependencies = dependencies();
        Car car = car();
        when(dependencies.maintenanceRepository.findAll())
                .thenReturn(Collections.singletonList(record(MaintenanceStatus.PENDING)));
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car));
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.of(mock(Rental.class)));

        dependencies.service.checkMaintenance(date(10));

        assertEquals(VehicleStatus.AVAILABLE, car.getStatus());
        verify(dependencies.vehicleRepository, never()).updateVehicle(car);
    }

    @Test
    void shouldRejectNullCompletionDate() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.completeMaintenance("V1", null));
    }

    @Test
    void shouldRejectMissingPendingMaintenance() {
        Dependencies dependencies = dependencies();
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car()));
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                dependencies.service.completeMaintenance("V1", date(10)));
    }

    @Test
    void shouldRejectCompletionBeforeLastMaintenanceDate() {
        Dependencies dependencies = readyForCompletion(car());

        assertThrows(IllegalArgumentException.class, () ->
                dependencies.service.completeMaintenance("V1", date(1)));
    }

    @Test
    void shouldUseAvailabilityServiceAfterCompletion() {
        Dependencies dependencies = readyForCompletion(car());

        MaintenanceRecord nextRecord = dependencies.service.completeMaintenance(
                "V1", date(12)
        );

        assertEquals(date(12).plusMonths(6), nextRecord.getNextMaintenanceDate());
        verify(dependencies.availabilityService).applyStatus(
                dependencies.vehicle, date(12)
        );
    }

    @Test
    void shouldMakeVehicleRentedAfterCompletionWhenRentalIsActive() {
        Dependencies dependencies = readyForCompletionWithoutAvailability(car());
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.of(mock(Rental.class)));

        dependencies.service.completeMaintenance("V1", date(12));

        assertEquals(VehicleStatus.RENTED, dependencies.vehicle.getStatus());
    }

    @Test
    void shouldKeepMaintenanceAfterCompletionWhenAccidentIsPending() {
        Dependencies dependencies = readyForCompletionWithoutAvailability(car());
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        when(dependencies.incidentRepository.hasPendingAccident("V1"))
                .thenReturn(true);

        dependencies.service.completeMaintenance("V1", date(12));

        assertEquals(VehicleStatus.MAINTENANCE, dependencies.vehicle.getStatus());
    }

    @Test
    void shouldMakeLowBatteryVehicleUnavailableAfterCompletion() {
        ElectricVehicle electricVehicle = new ElectricVehicle(
                "V1", "Tesla", "3", 100, VehicleStatus.MAINTENANCE, 20
        );
        Dependencies dependencies = readyForCompletionWithoutAvailability(electricVehicle);
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        when(dependencies.incidentRepository.hasPendingAccident("V1"))
                .thenReturn(false);

        dependencies.service.completeMaintenance("V1", date(12));

        assertEquals(VehicleStatus.UNAVAILABLE, electricVehicle.getStatus());
    }

    @Test
    void shouldReturnAllMaintenanceRecords() {
        Dependencies dependencies = dependencies();
        when(dependencies.maintenanceRepository.findAll())
                .thenReturn(Arrays.asList(record(MaintenanceStatus.PENDING)));

        assertEquals(1, dependencies.service.getAllMaintenanceRecords().size());
    }

    private Dependencies readyForCompletion(Vehicle vehicle) {
        Dependencies dependencies = dependencies();
        dependencies.vehicle = vehicle;
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(vehicle));
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record(MaintenanceStatus.PENDING)));
        return dependencies;
    }

    private Dependencies readyForCompletionWithoutAvailability(Vehicle vehicle) {
        Dependencies dependencies = new Dependencies(false);
        dependencies.vehicle = vehicle;
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(vehicle));
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record(MaintenanceStatus.PENDING)));
        return dependencies;
    }

    private Dependencies dependencies() {
        return new Dependencies(true);
    }

    private Car car() {
        return new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
    }

    private MaintenanceRecord record(MaintenanceStatus status) {
        return new MaintenanceRecord("M1", "V1", date(5), date(10), status);
    }

    private LocalDate date(int day) {
        return LocalDate.of(2026, 7, day);
    }

    private static class Dependencies {
        private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        private final RentalRepository rentalRepository = mock(RentalRepository.class);
        private final MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        private final VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        private final NotificationPublisher publisher = mock(NotificationPublisher.class);
        private final AuthenticationService authenticationService = mock(AuthenticationService.class);
        private final VehicleAvailabilityService availabilityService;
        private final MaintenanceService service;
        private Vehicle vehicle;

        private Dependencies(boolean useAvailabilityService) {
            availabilityService = useAvailabilityService
                    ? mock(VehicleAvailabilityService.class)
                    : null;
            service = new MaintenanceService(
                    vehicleRepository,
                    rentalRepository,
                    maintenanceRepository,
                    incidentRepository,
                    publisher,
                    authenticationService,
                    "manager@test.com",
                    availabilityService
            );
        }
    }
}
