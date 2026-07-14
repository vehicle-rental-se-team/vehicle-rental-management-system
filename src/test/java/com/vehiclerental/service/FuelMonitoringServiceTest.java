package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FuelMonitoringServiceTest {

    @Test
    void shouldSaveLowFuelAndSendNotification() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleFuelRepository fuelRepository = mock(VehicleFuelRepository.class);
        VehicleAvailabilityService availabilityService = mock(VehicleAvailabilityService.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(fuelRepository.findByVehicleId("V1")).thenReturn(Optional.empty());

        FuelMonitoringService service = new FuelMonitoringService(
                vehicleRepository, fuelRepository, availabilityService,
                publisher, mock(AuthenticationService.class),
                "manager@test.com");

        FuelRecord result = service.updateFuelLevel(
                "V1", 10, LocalDate.of(2026, 7, 1));

        assertEquals(10, result.getFuelLevel());
        verify(fuelRepository).saveOrUpdate(any(FuelRecord.class));
        verify(publisher).notifyObservers(
                eq("manager@test.com"), contains("low fuel"));
    }

    @Test
    void shouldRejectFuelUpdateForElectricVehicle() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.AVAILABLE, 80);
        when(vehicleRepository.findById("E1")).thenReturn(Optional.of(vehicle));

        FuelMonitoringService service = new FuelMonitoringService(
                vehicleRepository,
                mock(VehicleFuelRepository.class),
                mock(VehicleAvailabilityService.class),
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com");

        assertThrows(IllegalArgumentException.class, () ->
                service.updateFuelLevel("E1", 50, LocalDate.of(2026, 7, 1)));
    }
}
