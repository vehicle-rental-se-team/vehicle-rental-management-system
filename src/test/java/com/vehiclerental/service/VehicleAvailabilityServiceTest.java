package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleIncidentRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleAvailabilityServiceTest {

    @Test
    void shouldReturnRentedWhenActiveRentalExists() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.of(mock(Rental.class)));
        VehicleAvailabilityService service = createService(rentalRepository);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        assertEquals(VehicleStatus.RENTED,
                service.determineStatus(car, LocalDate.of(2026, 7, 1)));
    }

    @Test
    void shouldReturnMaintenanceWhenPendingAccidentExists() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        VehicleIncidentRepository incidentRepository = mock(VehicleIncidentRepository.class);
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        when(incidentRepository.hasPendingAccident("V1")).thenReturn(true);
        VehicleAvailabilityService service = new VehicleAvailabilityService(
                rentalRepository, mock(MaintenanceRepository.class),
                incidentRepository, mock(VehicleFuelRepository.class),
                mock(VehicleDocumentsRepository.class));
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        assertEquals(VehicleStatus.MAINTENANCE,
                service.determineStatus(car, LocalDate.of(2026, 7, 1)));
    }

    @Test
    void shouldReturnUnavailableForLowElectricBattery() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        when(rentalRepository.findActiveRentalByVehicleId("E1"))
                .thenReturn(Optional.empty());
        VehicleAvailabilityService service = createService(rentalRepository);
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.AVAILABLE, 20);

        assertEquals(VehicleStatus.UNAVAILABLE,
                service.determineStatus(vehicle, LocalDate.of(2026, 7, 1)));
    }

    @Test
    void shouldReturnAvailableWhenNoBlockerExists() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        VehicleAvailabilityService service = createService(rentalRepository);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.UNAVAILABLE);

        assertEquals(VehicleStatus.AVAILABLE,
                service.determineStatus(car, LocalDate.of(2026, 7, 1)));
    }

    private VehicleAvailabilityService createService(RentalRepository rentalRepository) {
        return new VehicleAvailabilityService(
                rentalRepository,
                null,
                null,
                null,
                null);
    }

    @Test
    void shouldReturnUnavailableForLowFuel() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        VehicleFuelRepository fuelRepository = mock(VehicleFuelRepository.class);
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        when(fuelRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(
                        new com.vehiclerental.domain.FuelRecord("V1", 10)
                ));
        VehicleAvailabilityService service = new VehicleAvailabilityService(
                rentalRepository,
                null,
                null,
                fuelRepository,
                null
        );
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        assertEquals(VehicleStatus.UNAVAILABLE,
                service.determineStatus(car, LocalDate.of(2026, 7, 1)));
    }

    @Test
    void shouldReturnUnavailableForExpiredDocuments() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        when(documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(
                        new com.vehiclerental.domain.VehicleDocuments(
                                "V1",
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2027, 1, 1)
                        )
                ));
        VehicleAvailabilityService service = new VehicleAvailabilityService(
                rentalRepository,
                null,
                null,
                null,
                documentsRepository
        );
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        assertEquals(VehicleStatus.UNAVAILABLE,
                service.determineStatus(car, LocalDate.of(2026, 7, 1)));
    }

    @Test
    void shouldApplyStatusToVehicle() {
        RentalRepository rentalRepository = mock(RentalRepository.class);
        when(rentalRepository.findActiveRentalByVehicleId("V1"))
                .thenReturn(Optional.empty());
        VehicleAvailabilityService service = createService(rentalRepository);
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.UNAVAILABLE);

        VehicleStatus status = service.applyStatus(
                car, LocalDate.of(2026, 7, 1)
        );

        assertEquals(VehicleStatus.AVAILABLE, status);
        assertEquals(VehicleStatus.AVAILABLE, car.getStatus());
    }

    @Test
    void shouldRejectNullVehicleOrDate() {
        VehicleAvailabilityService service = createService(
                mock(RentalRepository.class)
        );
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class,
                () -> service.determineStatus(null, LocalDate.of(2026, 7, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> service.determineStatus(car, null));
    }

}
