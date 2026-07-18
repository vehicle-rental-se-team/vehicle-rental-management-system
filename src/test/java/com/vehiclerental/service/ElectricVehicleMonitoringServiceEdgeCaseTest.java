package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElectricVehicleMonitoringServiceEdgeCaseTest {

    @Test
    void shouldRejectNullVehicleRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new ElectricVehicleMonitoringService(
                        null,
                        mock(RentalRepository.class),
                        mock(NotificationPublisher.class),
                        "manager@test.com"
                ));
    }

    @Test
    void shouldRejectNullRentalRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new ElectricVehicleMonitoringService(
                        mock(VehicleRepository.class),
                        null,
                        mock(NotificationPublisher.class),
                        "manager@test.com"
                ));
    }

    @Test
    void shouldRejectNullPublisher() {
        assertThrows(IllegalArgumentException.class, () ->
                new ElectricVehicleMonitoringService(
                        mock(VehicleRepository.class),
                        mock(RentalRepository.class),
                        null,
                        "manager@test.com"
                ));
    }

    @Test
    void shouldRejectBlankRecipient() {
        assertThrows(IllegalArgumentException.class, () ->
                new ElectricVehicleMonitoringService(
                        mock(VehicleRepository.class),
                        mock(RentalRepository.class),
                        mock(NotificationPublisher.class),
                        " "
                ));
    }

    @Test
    void shouldReturnFalseForNullBatteryChecks() {
        ElectricVehicleMonitoringService service = service().service;

        assertFalse(service.isBatteryLow(null));
        assertFalse(service.isBatteryEmpty(null));
    }

    @Test
    void shouldNotTreatThirtyPercentAsLow() {
        ElectricVehicle vehicle = electricVehicle(30);

        assertFalse(service().service.isBatteryLow(vehicle));
    }

    @Test
    void shouldUseAvailabilityServiceWhenProvided() {
        Dependencies dependencies = service(true);
        ElectricVehicle vehicle = electricVehicle(50);
        when(dependencies.vehicleRepository.findById("E1"))
                .thenReturn(Optional.of(vehicle));
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("E1"))
                .thenReturn(Optional.empty());

        dependencies.service.updateBatteryLevel("E1", 40);

        verify(dependencies.availabilityService).applyStatus(
                eq(vehicle), org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void shouldMentionUnavailableWhenEmptyVehicleIsNotRented() {
        Dependencies dependencies = service();
        ElectricVehicle vehicle = electricVehicle(50);
        when(dependencies.vehicleRepository.findById("E1"))
                .thenReturn(Optional.of(vehicle));
        when(dependencies.rentalRepository.findActiveRentalByVehicleId("E1"))
                .thenReturn(Optional.empty());

        dependencies.service.updateBatteryLevel("E1", 0);

        verify(dependencies.publisher).notifyObservers(
                eq("manager@test.com"), contains("now unavailable")
        );
    }

    @Test
    void shouldDetectEmptyBattery() {
        assertTrue(service().service.isBatteryEmpty(electricVehicle(0)));
    }

    private Dependencies service() {
        return service(false);
    }

    private Dependencies service(boolean useAvailability) {
        return new Dependencies(useAvailability);
    }

    private ElectricVehicle electricVehicle(int battery) {
        return new ElectricVehicle(
                "E1", "Tesla", "3", 100,
                VehicleStatus.UNAVAILABLE, battery
        );
    }

    private static class Dependencies {
        private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        private final RentalRepository rentalRepository = mock(RentalRepository.class);
        private final NotificationPublisher publisher = mock(NotificationPublisher.class);
        private final VehicleAvailabilityService availabilityService;
        private final ElectricVehicleMonitoringService service;

        private Dependencies(boolean useAvailability) {
            availabilityService = useAvailability
                    ? mock(VehicleAvailabilityService.class)
                    : null;
            service = new ElectricVehicleMonitoringService(
                    vehicleRepository,
                    rentalRepository,
                    publisher,
                    "manager@test.com",
                    availabilityService
            );
        }
    }
}
