package com.vehiclerental.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainModelTest {

    @Test
    void shouldCloseRental() {
        Rental rental = createRental();

        rental.close();

        assertFalse(rental.isActive());
        assertTrue(rental.isReturned());
        assertEquals(3, rental.getRentalDays());
    }

    @Test
    void shouldRejectRentalWithoutVehicle() {
        Customer customer = new Customer("C1", "Ahmad", "ahmad@test.com");

        assertThrows(IllegalArgumentException.class, () ->
                new Rental(
                        "R1",
                        null,
                        customer,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 4)
                ));
    }

    @Test
    void shouldValidateElectricBatteryLevel() {
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "Model 3", 100,
                VehicleStatus.AVAILABLE, 80
        );

        vehicle.setBatteryLevel(30);

        assertEquals(30, vehicle.getBatteryLevel());
        assertThrows(IllegalArgumentException.class,
                () -> vehicle.setBatteryLevel(101));
    }

    @Test
    void shouldValidateFuelLevel() {
        FuelRecord record = new FuelRecord("V1", 80);

        record.setFuelLevel(20);

        assertEquals(20, record.getFuelLevel());
        assertThrows(IllegalArgumentException.class,
                () -> record.setFuelLevel(-1));
    }

    @Test
    void shouldCompleteMaintenanceRecord() {
        MaintenanceRecord record = new MaintenanceRecord(
                "M1",
                "V1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 7, 1),
                MaintenanceStatus.PENDING
        );

        record.complete();

        assertFalse(record.isPending());
        assertEquals(MaintenanceStatus.COMPLETED, record.getStatus());
    }

    @Test
    void shouldUpdateVehicleDocuments() {
        VehicleDocuments documents = new VehicleDocuments(
                "V1",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 1)
        );

        documents.updateDates(
                LocalDate.of(2027, 7, 1),
                LocalDate.of(2027, 8, 1)
        );

        assertEquals(LocalDate.of(2027, 7, 1),
                documents.getRegistrationExpiryDate());
        assertFalse(documents.isRegistrationExpired(
                LocalDate.of(2027, 6, 30)));
        assertTrue(documents.isInsuranceExpired(
                LocalDate.of(2027, 8, 1)));
    }

    @Test
    void shouldCompleteAccidentInspection() {
        VehicleIncident incident = new VehicleIncident(
                "I1",
                "V1",
                IncidentType.ACCIDENT,
                LocalDate.of(2026, 7, 1),
                "Damage"
        );

        assertTrue(incident.requiresInspection());
        incident.completeInspection();

        assertFalse(incident.requiresInspection());
        assertTrue(incident.isInspectionCompleted());
    }

    @Test
    void shouldKeepViolationInspectionCompleted() {
        VehicleIncident incident = new VehicleIncident(
                "I1",
                "V1",
                IncidentType.VIOLATION,
                LocalDate.of(2026, 7, 1),
                "Speeding"
        );

        incident.completeInspection();

        assertTrue(incident.isInspectionCompleted());
    }

    @Test
    void shouldReportVehicleTypeAndAvailability() {
        Car car = new Car(
                "V1", "Toyota", "Corolla", 50,
                VehicleStatus.AVAILABLE
        );

        assertEquals("CAR", car.getType());
        assertTrue(car.isAvailable());
        assertTrue(car.toString().contains("Toyota"));
    }

    private Rental createRental() {
        Car car = new Car(
                "V1", "Toyota", "Corolla", 50,
                VehicleStatus.RENTED
        );
        Customer customer = new Customer(
                "C1", "Ahmad", "ahmad@test.com"
        );
        return new Rental(
                "R1",
                car,
                customer,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 4)
        );
    }
}
