package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.IncidentType;
import com.vehiclerental.domain.VehicleIncident;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleIncidentRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleIncidentServiceTest {

    @Test
    void shouldRecordAccidentAndSendVehicleToMaintenance() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));

        VehicleIncidentService service = new VehicleIncidentService(
                vehicleRepository, incidentRepository, publisher,
                mock(AuthenticationService.class), "manager@test.com");

        VehicleIncident incident = service.recordIncident(
                "V1", IncidentType.ACCIDENT,
                LocalDate.of(2026, 7, 1), "Front damage");

        assertTrue(incident.requiresInspection());
        assertEquals(VehicleStatus.MAINTENANCE, car.getStatus());
        verify(incidentRepository).save(incident);
        verify(vehicleRepository).updateVehicle(car);
        verify(publisher).notifyObservers(
                eq("manager@test.com"), contains("accident"));
    }

    @Test
    void shouldRecordViolationWithoutChangingVehicleStatus() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));

        VehicleIncidentService service = new VehicleIncidentService(
                vehicleRepository, incidentRepository,
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class), "manager@test.com");

        service.recordIncident(
                "V1", IncidentType.VIOLATION,
                LocalDate.of(2026, 7, 1), "Speeding");

        assertEquals(VehicleStatus.AVAILABLE, car.getStatus());
        verify(vehicleRepository, never()).updateVehicle(car);
    }

    @Test
    void shouldRejectBlankIncidentDescription() {
        VehicleIncidentService service = new VehicleIncidentService(
                mock(VehicleRepository.class),
                mock(VehicleIncidentRepository.class),
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.recordIncident(
                        "V1",
                        IncidentType.ACCIDENT,
                        LocalDate.of(2026, 7, 1),
                        " "
                ));
    }

    @Test
    void shouldRejectInspectionWhenNoPendingAccidentExists() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.MAINTENANCE);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(incidentRepository.findPendingAccidentsByVehicleId("V1"))
                .thenReturn(java.util.Collections.emptyList());

        VehicleIncidentService service = new VehicleIncidentService(
                vehicleRepository,
                incidentRepository,
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        assertThrows(IllegalStateException.class, () ->
                service.completeInspection(
                        "V1", LocalDate.of(2026, 7, 2)
                ));
    }

    @Test
    void shouldCompletePendingAccidentInspection() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.MAINTENANCE);
        VehicleIncident accident = new VehicleIncident(
                "I1",
                "V1",
                IncidentType.ACCIDENT,
                LocalDate.of(2026, 7, 1),
                "Damage"
        );
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(incidentRepository.findPendingAccidentsByVehicleId("V1"))
                .thenReturn(java.util.Collections.singletonList(accident));

        VehicleIncidentService service = new VehicleIncidentService(
                vehicleRepository,
                incidentRepository,
                publisher,
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        int count = service.completeInspection(
                "V1", LocalDate.of(2026, 7, 2)
        );

        assertEquals(1, count);
        assertTrue(accident.isInspectionCompleted());
        assertEquals(VehicleStatus.AVAILABLE, car.getStatus());
        verify(incidentRepository).update(accident);
        verify(vehicleRepository).updateVehicle(car);
    }

}
