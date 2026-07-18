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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleDocumentsServiceEdgeCaseTest {

    @Test
    void shouldRejectMissingDependency() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new VehicleDocumentsService(
                        null,
                        dependencies.documentsRepository,
                        dependencies.availabilityService,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        "manager@test.com"
                ));
    }

    @Test
    void shouldRejectBlankNotificationRecipient() {
        Dependencies dependencies = dependencies();

        assertThrows(IllegalArgumentException.class, () ->
                new VehicleDocumentsService(
                        dependencies.vehicleRepository,
                        dependencies.documentsRepository,
                        dependencies.availabilityService,
                        dependencies.publisher,
                        dependencies.authenticationService,
                        " "
                ));
    }

    @Test
    void shouldRejectBlankVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                dependencies().service.getDocuments(" "));
    }

    @Test
    void shouldRejectUnknownVehicle() {
        Dependencies dependencies = dependencies();
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                dependencies.service.getDocuments("V1"));
    }

    @Test
    void shouldUpdateExistingDocuments() {
        Dependencies dependencies = dependencies();
        Car car = car();
        VehicleDocuments documents = new VehicleDocuments(
                "V1", date(10), date(11)
        );
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car));
        when(dependencies.documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(documents));

        VehicleDocuments result = dependencies.service.updateDocuments(
                "V1", date(20), date(21)
        );

        assertEquals(date(20), result.getRegistrationExpiryDate());
        assertEquals(date(21), result.getInsuranceExpiryDate());
    }

    @Test
    void shouldSendInsuranceReminderFiveDaysBeforeExpiry() {
        Dependencies dependencies = dependencies();
        Car car = car();
        VehicleDocuments documents = new VehicleDocuments(
                "V1", date(20), date(15)
        );
        when(dependencies.documentsRepository.findAll())
                .thenReturn(Collections.singletonList(documents));
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car));

        int count = dependencies.service.checkDocuments(date(10));

        assertEquals(1, count);
        verify(dependencies.publisher).notifyObservers(
                eq("manager@test.com"), contains("Insurance")
        );
    }

    @Test
    void shouldReturnDocumentsWhenTheyExist() {
        Dependencies dependencies = dependencies();
        VehicleDocuments documents = new VehicleDocuments(
                "V1", date(20), date(21)
        );
        when(dependencies.vehicleRepository.findById("V1"))
                .thenReturn(Optional.of(car()));
        when(dependencies.documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(documents));

        assertEquals(documents, dependencies.service.getDocuments("V1"));
    }

    private Dependencies dependencies() {
        return new Dependencies();
    }

    private Car car() {
        return new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
    }

    private LocalDate date(int day) {
        return LocalDate.of(2026, 7, day);
    }

    private static class Dependencies {
        private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        private final VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        private final VehicleAvailabilityService availabilityService = mock(VehicleAvailabilityService.class);
        private final NotificationPublisher publisher = mock(NotificationPublisher.class);
        private final AuthenticationService authenticationService = mock(AuthenticationService.class);
        private final VehicleDocumentsService service = new VehicleDocumentsService(
                vehicleRepository,
                documentsRepository,
                availabilityService,
                publisher,
                authenticationService,
                "manager@test.com"
        );
    }
}
