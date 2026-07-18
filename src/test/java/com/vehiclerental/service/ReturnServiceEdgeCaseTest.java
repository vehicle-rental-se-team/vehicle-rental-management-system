package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReturnServiceEdgeCaseTest {

    @Test
    void shouldCreateServiceUsingMaintenanceConstructor() {
        ReturnService service = new ReturnService(
                mock(RentalRepository.class),
                mock(BillingService.class),
                mock(AuthenticationService.class),
                mock(MaintenanceRepository.class)
        );

        assertNotNull(service);
    }

    @Test
    void shouldCreateServiceUsingVehicleRepositoryConstructor() {
        ReturnService service = new ReturnService(
                mock(RentalRepository.class),
                mock(BillingService.class),
                mock(AuthenticationService.class),
                mock(MaintenanceRepository.class),
                mock(VehicleRepository.class)
        );

        assertNotNull(service);
    }

    @Test
    void shouldReturnEmptyForBlankRentalId() {
        ReturnService service = service().service;

        assertFalse(service.findActiveRental(" ").isPresent());
    }

    @Test
    void shouldReturnEmptyWhenRentalDoesNotExist() {
        Dependencies dependencies = service();
        when(dependencies.rentalRepository.findById("R1"))
                .thenReturn(Optional.empty());

        assertFalse(dependencies.service.findActiveRental("R1").isPresent());
    }

    @Test
    void shouldReturnActiveRental() {
        Dependencies dependencies = service();
        Rental rental = rental(car());
        when(dependencies.rentalRepository.findById("R1"))
                .thenReturn(Optional.of(rental));

        assertEquals(rental, dependencies.service.findActiveRental("R1").orElse(null));
    }

    @Test
    void shouldUseAvailabilityServiceAfterReturn() {
        Dependencies dependencies = service(true);
        Rental rental = rental(car());
        allowReturn(dependencies, rental);

        dependencies.service.returnVehicle("R1", date(5));

        verify(dependencies.availabilityService).applyStatus(
                rental.getVehicle(), date(5)
        );
    }

    @Test
    void shouldKeepMaintenanceStatusAfterReturn() {
        Dependencies dependencies = service(false);
        Car car = car();
        car.setStatus(VehicleStatus.MAINTENANCE);
        Rental rental = rental(car);
        allowReturn(dependencies, rental);

        dependencies.service.returnVehicle("R1", date(5));

        assertEquals(VehicleStatus.MAINTENANCE, car.getStatus());
    }

    @Test
    void shouldSetMaintenanceWhenMaintenanceIsDue() {
        Dependencies dependencies = service(false);
        Car car = car();
        Rental rental = rental(car);
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1", date(1), date(4), MaintenanceStatus.PENDING
        );
        allowReturn(dependencies, rental);
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record));

        dependencies.service.returnVehicle("R1", date(5));

        assertEquals(VehicleStatus.MAINTENANCE, car.getStatus());
    }

    @Test
    void shouldMakeLowBatteryVehicleUnavailableAfterReturn() {
        Dependencies dependencies = service(false);
        ElectricVehicle vehicle = new ElectricVehicle(
                "V1", "Tesla", "3", 100, VehicleStatus.RENTED, 20
        );
        Rental rental = rental(vehicle);
        allowReturn(dependencies, rental);
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.empty());

        dependencies.service.returnVehicle("R1", date(5));

        assertEquals(VehicleStatus.UNAVAILABLE, vehicle.getStatus());
    }

    private void allowReturn(Dependencies dependencies, Rental rental) {
        when(dependencies.rentalRepository.findById("R1"))
                .thenReturn(Optional.of(rental));
        when(dependencies.billingService.calculateTotalCost(rental, date(5)))
                .thenReturn(200.0);
    }

    private Dependencies service() {
        return service(false);
    }

    private Dependencies service(boolean useAvailability) {
        return new Dependencies(useAvailability);
    }

    private Rental rental(Vehicle vehicle) {
        return new Rental(
                "R1",
                vehicle,
                new Customer("C1", "Ahmad", "a@test.com"),
                date(1),
                date(4)
        );
    }

    private Car car() {
        return new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.RENTED);
    }

    private LocalDate date(int day) {
        return LocalDate.of(2026, 7, day);
    }

    private static class Dependencies {
        private final RentalRepository rentalRepository = mock(RentalRepository.class);
        private final BillingService billingService = mock(BillingService.class);
        private final AuthenticationService authenticationService = mock(AuthenticationService.class);
        private final MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        private final VehicleAvailabilityService availabilityService;
        private final ReturnService service;

        private Dependencies(boolean useAvailability) {
            availabilityService = useAvailability
                    ? mock(VehicleAvailabilityService.class)
                    : null;
            service = new ReturnService(
                    rentalRepository,
                    billingService,
                    authenticationService,
                    maintenanceRepository,
                    vehicleRepository,
                    availabilityService
            );
        }
    }
}
