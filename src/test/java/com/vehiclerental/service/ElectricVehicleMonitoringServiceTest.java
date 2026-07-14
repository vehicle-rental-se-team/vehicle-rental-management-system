package com.vehiclerental.service;

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
}
