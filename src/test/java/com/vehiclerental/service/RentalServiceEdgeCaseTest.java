package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.MaintenanceRecord;
import com.vehiclerental.domain.MaintenanceStatus;
import com.vehiclerental.domain.Motorcycle;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.Truck;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleDocuments;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.exception.InvalidRentalPeriodException;
import com.vehiclerental.exception.VehicleNotAvailableException;
import com.vehiclerental.repository.MaintenanceRepository;
import com.vehiclerental.repository.RentalRepository;
import com.vehiclerental.repository.VehicleDocumentsRepository;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RentalServiceEdgeCaseTest {

    @Test
    void shouldRejectNullVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                service().rentVehicle(null, "Ahmad", "a@test.com", 25,
                        false, date(1), date(2)));
    }

    @Test
    void shouldRejectBlankCustomerName() {
        assertThrows(IllegalArgumentException.class, () ->
                service().rentVehicle("V1", " ", "a@test.com", 25,
                        false, date(1), date(2)));
    }

    @Test
    void shouldRejectBlankCustomerEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                service().rentVehicle("V1", "Ahmad", " ", 25,
                        false, date(1), date(2)));
    }

    @Test
    void shouldRejectZeroCustomerAge() {
        assertThrows(IllegalArgumentException.class, () ->
                service().rentVehicle("V1", "Ahmad", "a@test.com", 0,
                        false, date(1), date(2)));
    }

    @Test
    void shouldRejectNullStartDate() {
        assertThrows(InvalidRentalPeriodException.class, () ->
                service().rentVehicle("V1", "Ahmad", "a@test.com", 25,
                        false, null, date(2)));
    }

    @Test
    void shouldRejectNullEndDate() {
        assertThrows(InvalidRentalPeriodException.class, () ->
                service().rentVehicle("V1", "Ahmad", "a@test.com", 25,
                        false, date(1), null));
    }

    @Test
    void shouldRejectMaintenanceConflict() {
        TestDependencies dependencies = dependencies();
        Car car = car();
        MaintenanceRecord record = new MaintenanceRecord(
                "M1", "V1", date(1), date(5), MaintenanceStatus.PENDING
        );
        allowRental(dependencies, car);
        when(dependencies.maintenanceRepository.findPendingByVehicleId("V1"))
                .thenReturn(Optional.of(record));

        assertThrows(InvalidRentalPeriodException.class, () ->
                dependencies.service.rentVehicle(
                        "V1", "Ahmad", "a@test.com", 25,
                        false, date(2), date(6)));
    }

    @Test
    void shouldRejectLowFuel() {
        TestDependencies dependencies = dependencies();
        allowRental(dependencies, car());
        when(dependencies.fuelRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(new FuelRecord("V1", 10)));

        assertThrows(VehicleNotAvailableException.class, () ->
                dependencies.service.rentVehicle(
                        "V1", "Ahmad", "a@test.com", 25,
                        false, date(1), date(3)));
    }

    @Test
    void shouldRejectRegistrationExpiringDuringRental() {
        TestDependencies dependencies = dependencies();
        allowRental(dependencies, car());
        when(dependencies.documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(new VehicleDocuments(
                        "V1", date(3), date(10)
                )));

        assertThrows(VehicleNotAvailableException.class, () ->
                dependencies.service.rentVehicle(
                        "V1", "Ahmad", "a@test.com", 25,
                        false, date(1), date(3)));
    }

    @Test
    void shouldRejectInsuranceExpiringDuringRental() {
        TestDependencies dependencies = dependencies();
        allowRental(dependencies, car());
        when(dependencies.documentsRepository.findByVehicleId("V1"))
                .thenReturn(Optional.of(new VehicleDocuments(
                        "V1", date(10), date(3)
                )));

        assertThrows(VehicleNotAvailableException.class, () ->
                dependencies.service.rentVehicle(
                        "V1", "Ahmad", "a@test.com", 25,
                        false, date(1), date(3)));
    }

    @Test
    void shouldRentTruckWithSpecialLicense() {
        Truck truck = new Truck("T1", "Volvo", "FH", 200, VehicleStatus.AVAILABLE);

        Rental rental = rentVehicle(truck, 25, true);

        assertNotNull(rental);
    }

    @Test
    void shouldRentMotorcycleForAdultCustomer() {
        Motorcycle motorcycle = new Motorcycle(
                "M1", "Honda", "CBR", 40, VehicleStatus.AVAILABLE
        );

        Rental rental = rentVehicle(motorcycle, 21, false);

        assertNotNull(rental);
    }

    @Test
    void shouldRentElectricVehicleWithEnoughBattery() {
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "3", 100, VehicleStatus.AVAILABLE, 30
        );

        Rental rental = rentVehicle(vehicle, 25, false);

        assertNotNull(rental);
    }

    @Test
    void shouldUseSimpleRentalMethod() {
        TestDependencies dependencies = dependencies();
        allowRental(dependencies, car());

        Rental rental = dependencies.service.rentVehicle(
                "V1", "Ahmad", "a@test.com", date(1), date(2)
        );

        assertNotNull(rental);
    }

    private Rental rentVehicle(Vehicle vehicle, int age, boolean license) {
        TestDependencies dependencies = dependencies();
        allowRental(dependencies, vehicle);
        return dependencies.service.rentVehicle(
                vehicle.getId(), "Ahmad", "a@test.com", age,
                license, date(1), date(2)
        );
    }

    private void allowRental(TestDependencies dependencies, Vehicle vehicle) {
        when(dependencies.vehicleRepository.findById(vehicle.getId()))
                .thenReturn(Optional.of(vehicle));
        when(dependencies.rentalRepository.findActiveRentalByVehicleId(vehicle.getId()))
                .thenReturn(Optional.empty());
        when(dependencies.maintenanceRepository.findPendingByVehicleId(vehicle.getId()))
                .thenReturn(Optional.empty());
        when(dependencies.fuelRepository.findByVehicleId(vehicle.getId()))
                .thenReturn(Optional.empty());
        when(dependencies.documentsRepository.findByVehicleId(vehicle.getId()))
                .thenReturn(Optional.empty());
    }

    private RentalService service() {
        return dependencies().service;
    }

    private TestDependencies dependencies() {
        return new TestDependencies();
    }

    private Car car() {
        return new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
    }

    private LocalDate date(int day) {
        return LocalDate.of(2026, 7, day);
    }

    private static class TestDependencies {
        private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        private final RentalRepository rentalRepository = mock(RentalRepository.class);
        private final MaintenanceRepository maintenanceRepository = mock(MaintenanceRepository.class);
        private final VehicleFuelRepository fuelRepository = mock(VehicleFuelRepository.class);
        private final VehicleDocumentsRepository documentsRepository = mock(VehicleDocumentsRepository.class);
        private final RentalService service = new RentalService(
                vehicleRepository,
                rentalRepository,
                mock(AuthenticationService.class),
                maintenanceRepository,
                fuelRepository,
                documentsRepository
        );
    }
}
