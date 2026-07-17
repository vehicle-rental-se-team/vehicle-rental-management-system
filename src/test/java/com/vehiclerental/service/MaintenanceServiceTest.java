package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaintenanceServiceTest {

    @Test
    void shouldScheduleNextMaintenanceAfterSixMonths() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.empty());

        MaintenanceService service = new MaintenanceService(
                vehicleRepository,
                mock(RentalRepository.class),
                maintenanceRepository,
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com");

        MaintenanceRecord record = service.scheduleMaintenance(
                "V1", LocalDate.of(2026, 1, 10));

        assertEquals(LocalDate.of(2026, 7, 10), record.getNextMaintenanceDate());
        assertTrue(record.isPending());
        verify(maintenanceRepository).save(record);
    }

    @Test
    void shouldSendReminderFiveDaysBeforeMaintenance() {
        MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 7, 10),
                com.vehiclerental.domain.MaintenanceStatus.PENDING);
        when(maintenanceRepository.findAll())
                .thenReturn(Collections.singletonList(record));

        MaintenanceService service = new MaintenanceService(
                mock(VehicleRepository.class),
                mock(RentalRepository.class),
                maintenanceRepository,
                publisher,
                mock(AuthenticationService.class),
                "manager@test.com");

        int notifications = service.checkMaintenance(LocalDate.of(2026, 7, 5));

        assertEquals(1, notifications);
        verify(publisher).notifyObservers(
                eq("manager@test.com"), contains("requires maintenance"));
    }

    @Test
    void shouldRejectDuplicateMaintenanceSchedule() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1),
                com.vehiclerental.domain.MaintenanceStatus.PENDING
        );
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record));

        MaintenanceService service = new MaintenanceService(
                vehicleRepository,
                mock(RentalRepository.class),
                maintenanceRepository,
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        assertThrows(IllegalStateException.class, () ->
                service.scheduleMaintenance(
                        "V1", LocalDate.of(2026, 1, 1)
                ));
    }

    @Test
    void shouldMarkVehicleForMaintenanceOnDueDate() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        RentalRepository rentalRepository = mock(RentalRepository.class);
        MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1),
                com.vehiclerental.domain.MaintenanceStatus.PENDING
        );
        when(maintenanceRepository.findAll())
                .thenReturn(Collections.singletonList(record));
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());

        MaintenanceService service = new MaintenanceService(
                vehicleRepository,
                rentalRepository,
                maintenanceRepository,
                publisher,
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        int count = service.checkMaintenance(LocalDate.of(2026, 7, 1));

        assertEquals(1, count);
        assertEquals(VehicleStatus.MAINTENANCE, car.getStatus());
        verify(vehicleRepository).updateVehicle(car);
        verify(publisher).notifyObservers(
                eq("manager@test.com"), contains("due for maintenance today")
        );
    }

    @Test
    void shouldRejectNullDateWhenCheckingMaintenance() {
        MaintenanceService service = new MaintenanceService(
                mock(VehicleRepository.class),
                mock(RentalRepository.class),
                mock(MaintenanceRepository.class),
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.checkMaintenance(null));
    }

}
