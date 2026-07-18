package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FuelMonitoringServiceEdgeCaseTest {

    @Test
    void shouldRejectMissingDependency() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new FuelMonitoringService(
                        null,
                        dependencies.fuelRepository,
                        dependencies.availabilityService,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        "manager@test.com"
                ));
    }

    @Test
    void shouldRejectBlankRecipient() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new FuelMonitoringService(
                        dependencies.vehicleRepository,
                        dependencies.fuelRepository,
                        dependencies.availabilityService,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        " "
                ));
    }

    @Test
    void shouldRejectBlankVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.getFuelLevel(" "));
    }

    @Test
    void shouldRejectUnknownVehicle() {
        Dependencies dependencies = dependencies();
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                dependencies.service.getFuelLevel("V1"));
    }

    @Test
    void shouldUpdateExistingFuelRecord() {
        Dependencies dependencies = dependencies();
        Car car = car();
        FuelRecord record = new FuelRecord("V1", 80);
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car));
        when(dependencies.fuelRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(record));

        FuelRecord result = dependencies.service.updateFuelLevel(
                "V1", 60, LocalDate.of(2026, 7, 10)
        );

        assertEquals(60, result.getFuelLevel());
        verify(dependencies.fuelRepository).saveOrUpdate(record);
    }

    @Test
    void shouldUseCurrentDateUpdateMethod() {
        Dependencies dependencies = dependencies();
        Car car = car();
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car));
        when(dependencies.fuelRepository.findByVehicleId("V1"))
                .thenReturn(Optional.empty());

        FuelRecord result = dependencies.service.updateFuelLevel("V1", 50);

        assertEquals(50, result.getFuelLevel());
    }

    @Test
    void shouldReturnStoredFuelLevel() {
        Dependencies dependencies = dependencies();
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car()));
        when(dependencies.fuelRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(new FuelRecord("V1", 70)));

        assertEquals(70, dependencies.service.getFuelLevel("V1"));
    }

    private Dependencies dependencies() {
        return new Dependencies();
    }

    private Car car() {
        return new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
    }

    private static class Dependencies {
        private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        private final VehicleFuelRepository fuelRepository = mock(VehicleFuelRepository.class);
        private final VehicleAvailabilityService availabilityService = mock(VehicleAvailabilityService.class);
        private final NotificationPublisher publisher = mock(NotificationPublisher.class);
        private final AuthenticationService authenticationService = mock(AuthenticationService.class);
        private final FuelMonitoringService service = new FuelMonitoringService(
                vehicleRepository,
                fuelRepository,
                availabilityService,
                publisher,
                authenticationService,
                "manager@test.com"
        );
    }
}
