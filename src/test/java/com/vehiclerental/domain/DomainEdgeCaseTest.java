package com.vehiclerental.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainEdgeCaseTest {

    @Test
    void shouldRejectBlankMaintenanceId() {
        assertThrows(IllegalArgumentException.class, () ->
                maintenanceRecord(" ", "V1", date(1), date(2), MaintenanceStatus.PENDING));
    }

    @Test
    void shouldRejectBlankMaintenanceVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                maintenanceRecord("M1", " ", date(1), date(2), MaintenanceStatus.PENDING));
    }

    @Test
    void shouldRejectNullMaintenanceDates() {
        assertThrows(IllegalArgumentException.class, () ->
                maintenanceRecord("M1", "V1", null, date(2), MaintenanceStatus.PENDING));
    }

    @Test
    void shouldRejectInvalidMaintenanceDateOrder() {
        assertThrows(IllegalArgumentException.class, () ->
                maintenanceRecord("M1", "V1", date(2), date(1), MaintenanceStatus.PENDING));
    }

    @Test
    void shouldRejectNullMaintenanceStatus() {
        assertThrows(IllegalArgumentException.class, () ->
                maintenanceRecord("M1", "V1", date(1), date(2), null));
    }

    @Test
    void shouldRejectBlankRentalId() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rental(" ", vehicle(), customer(), date(1), date(2)));
    }

    @Test
    void shouldRejectRentalWithoutCustomer() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rental("R1", vehicle(), null, date(1), date(2)));
    }

    @Test
    void shouldRejectRentalWithoutDates() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rental("R1", vehicle(), customer(), null, date(2)));
    }

    @Test
    void shouldReportReturnedRental() {
        Rental rental = new Rental("R1", vehicle(), customer(), date(1), date(2));

        rental.close();

        assertTrue(rental.isReturned());
        assertFalse(rental.isActive());
    }

    @Test
    void shouldRejectBlankFuelVehicleId() {
        assertThrows(IllegalArgumentException.class, () -> new FuelRecord(" ", 50));
    }

    @Test
    void shouldRejectFuelBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> new FuelRecord("V1", -1));
    }

    @Test
    void shouldRejectFuelAboveOneHundred() {
        FuelRecord record = new FuelRecord("V1", 50);

        assertThrows(IllegalArgumentException.class, () -> record.setFuelLevel(101));
    }

    @Test
    void shouldRejectBatteryBelowZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new ElectricVehicle("E1", "Tesla", "3", 100,
                        VehicleStatus.AVAILABLE, -1));
    }

    @Test
    void shouldRejectBatteryAboveOneHundred() {
        ElectricVehicle vehicle = new ElectricVehicle(
                "E1", "Tesla", "3", 100, VehicleStatus.AVAILABLE, 50
        );

        assertThrows(IllegalArgumentException.class, () -> vehicle.setBatteryLevel(101));
    }

    @Test
    void shouldRejectBlankDocumentVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleDocuments(" ", date(2), date(3)));
    }

    @Test
    void shouldRejectNullDocumentDates() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleDocuments("V1", null, date(3)));
    }

    @Test
    void shouldRejectNullDatesWhenUpdatingDocuments() {
        VehicleDocuments documents = new VehicleDocuments("V1", date(2), date(3));

        assertThrows(IllegalArgumentException.class, () ->
                documents.updateDates(date(4), null));
    }

    @Test
    void shouldTreatExpiryDateAsExpired() {
        VehicleDocuments documents = new VehicleDocuments("V1", date(2), date(3));

        assertTrue(documents.isRegistrationExpired(date(2)));
        assertTrue(documents.isInsuranceExpired(date(3)));
    }

    @Test
    void shouldReturnStandardVehicleTypeAndText() {
        Vehicle vehicle = vehicle();

        assertEquals("STANDARD", vehicle.getType());
        assertTrue(vehicle.toString().contains("V1"));
    }

    private MaintenanceRecord maintenanceRecord(
            String id,
            String vehicleId,
            LocalDate lastDate,
            LocalDate nextDate,
            MaintenanceStatus status) {
        return new MaintenanceRecord(id, vehicleId, lastDate, nextDate, status);
    }

    private Vehicle vehicle() {
        return new Vehicle("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
    }

    private Customer customer() {
        return new Customer("C1", "Ahmad", "ahmad@test.com");
    }

    private LocalDate date(int day) {
        return LocalDate.of(2026, 7, day);
    }
}
