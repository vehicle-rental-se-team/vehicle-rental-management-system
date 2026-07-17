package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.VehicleDocuments;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleRepository;
import com.vehiclerental.service.notification.NotificationPublisher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleDocumentsServiceTest {

    @Test
    void shouldUpdateVehicleDocuments() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        VehicleAvailabilityService availabilityService = mock(VehicleAvailabilityService.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.empty());

        VehicleDocumentsService service = new VehicleDocumentsService(
                vehicleRepository, documentsRepository, availabilityService,
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com");

        VehicleDocuments documents = service.updateDocuments(
                "V1", LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 2, 1));

        assertEquals("V1", documents.getVehicleId());
        verify(documentsRepository).saveOrUpdate(documents);
        verify(vehicleRepository).updateVehicle(car);
    }

    @Test
    void shouldSendRegistrationReminderFiveDaysBeforeExpiry() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        VehicleDocuments documents = new VehicleDocuments(
                "V1", LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 10));
        when(documentsRepository.findAll())
                .thenReturn(Collections.singletonList(documents));
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));

        VehicleDocumentsService service = new VehicleDocumentsService(
                vehicleRepository, documentsRepository,
                mock(VehicleAvailabilityService.class), publisher,
                mock(AuthenticationService.class), "manager@test.com");

        int count = service.checkDocuments(LocalDate.of(2026, 7, 5));

        assertEquals(1, count);
        verify(publisher).notifyObservers(
                eq("manager@test.com"), contains("Registration"));
    }

    @Test
    void shouldSendExpiredDocumentNotifications() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        VehicleAvailabilityService availabilityService = mock(VehicleAvailabilityService.class);
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        VehicleDocuments documents = new VehicleDocuments(
                "V1",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1)
        );
        when(documentsRepository.findAll())
                .thenReturn(Collections.singletonList(documents));
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));

        VehicleDocumentsService service = new VehicleDocumentsService(
                vehicleRepository,
                documentsRepository,
                availabilityService,
                publisher,
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        int count = service.checkDocuments(LocalDate.of(2026, 7, 1));

        assertEquals(2, count);
        verify(publisher, times(2)).notifyObservers(
                eq("manager@test.com"), contains("expired")
        );
        verify(vehicleRepository).updateVehicle(car);
    }

    @Test
    void shouldRejectNullDateWhenCheckingDocuments() {
        VehicleDocumentsService service = new VehicleDocumentsService(
                mock(VehicleRepository.class),
                mock(VehicleDocumentsRepository.class),
                mock(VehicleAvailabilityService.class),
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.checkDocuments(null));
    }

    @Test
    void shouldRejectMissingVehicleDocuments() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(car));
        when(documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.empty());

        VehicleDocumentsService service = new VehicleDocumentsService(
                vehicleRepository,
                documentsRepository,
                mock(VehicleAvailabilityService.class),
                mock(NotificationPublisher.class),
                mock(AuthenticationService.class),
                "manager@test.com"
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.getDocuments("V1"));
    }

}
