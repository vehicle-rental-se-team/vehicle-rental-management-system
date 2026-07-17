package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElectricVehicleMonitoringServiceTest {

    @Test
    void shouldSendLowBatteryNotificationAndMakeVehicleUnavailable() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        RentalRepository rentalRepository = mock(RentalRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.AVAILABLE, 80);
        when(vehicleRepository.findById("E1")).thenReturn(Optional.of(vehicle));
        when(rentalRepository.findActiveRentalByVehicleId("E1"))
                .thenReturn(Optional.empty());

        VehicleAvailabilityService availabilityService =
                new VehicleAvailabilityService(
                        rentalRepository, null, null, null, null);
        ElectricVehicleMonitoringService service =
                new ElectricVehicleMonitoringService(
                        vehicleRepository, rentalRepository, publisher,
                        "manager@test.com", availabilityService);

        service.updateBatteryLevel("E1", 20);

        assertEquals(20, vehicle.getBatteryLevel());
        assertEquals(VehicleStatus.UNAVAILABLE, vehicle.getStatus());
        verify(publisher).notifyObservers(
                eq("manager@test.com"), contains("low battery"));
        verify(vehicleRepository).updateVehicle(vehicle);
    }

    @Test
    void shouldKeepRentedStatusWhenBatteryBecomesEmpty() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        RentalRepository rentalRepository = mock(RentalRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.RENTED, 80);
        when(vehicleRepository.findById("E1")).thenReturn(Optional.of(vehicle));
        when(rentalRepository.findActiveRentalByVehicleId("E1"))
                .thenReturn(Optional.of(mock(Rental.class)));

        VehicleAvailabilityService availabilityService =
                new VehicleAvailabilityService(
                        rentalRepository, null, null, null, null);
        ElectricVehicleMonitoringService service =
                new ElectricVehicleMonitoringService(
                        vehicleRepository, rentalRepository, publisher,
                        "manager@test.com", availabilityService);

        service.updateBatteryLevel("E1", 0);

        assertEquals(VehicleStatus.RENTED, vehicle.getStatus());
        verify(publisher).notifyObservers(
                eq("manager@test.com"), contains("empty battery"));
    }

    @Test
    void shouldRejectNonElectricVehicle() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        Car car = new Car(
                "V1", "Toyota", "Corolla", 50,
                VehicleStatus.AVAILABLE
        );
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));

        ElectricVehicleMonitoringService service =
                new ElectricVehicleMonitoringService(
                        vehicleRepository,
                        mock(RentalRepository.class),
                        mock(NotificationPublisher.class),
                        "manager@test.com"
                );

        assertThrows(IllegalArgumentException.class,
                () -> service.updateBatteryLevel("V1", 50));
    }

    @Test
    void shouldMakeVehicleAvailableAtThirtyPercent() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        RentalRepository rentalRepository = mock(RentalRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.UNAVAILABLE, 20
        );
        when(vehicleRepository.findById("E1")).thenReturn(Optional.of(vehicle));
        when(rentalRepository.findActiveRentalByVehicleId("E1"))
                .thenReturn(Optional.empty());

        ElectricVehicleMonitoringService service =
                new ElectricVehicleMonitoringService(
                        vehicleRepository,
                        rentalRepository,
                        publisher,
                        "manager@test.com"
                );

        service.updateBatteryLevel("E1", 30);

        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(publisher, never()).notifyObservers(anyString(), anyString());
    }

    @Test
    void shouldRejectUnknownElectricVehicle() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        when(vehicleRepository.findById("E9")).thenReturn(Optional.empty());

        ElectricVehicleMonitoringService service =
                new ElectricVehicleMonitoringService(
                        vehicleRepository,
                        mock(RentalRepository.class),
                        mock(NotificationPublisher.class),
                        "manager@test.com"
                );

        assertThrows(IllegalArgumentException.class,
                () -> service.updateBatteryLevel("E9", 50));
    }

}
