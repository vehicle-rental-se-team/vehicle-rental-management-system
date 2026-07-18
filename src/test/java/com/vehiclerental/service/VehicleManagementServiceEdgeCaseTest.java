package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Motorcycle;
import com.vehiclerental.domain.Truck;
import com.vehiclerental.domain.Van;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VehicleManagementServiceEdgeCaseTest {

    @Test
    void shouldRejectNullDependency() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleManagementService(
                        null,
                        mock(VehicleFuelRepository.class),
                        mock(AuthenticationService.class)
                ));
    }

    @Test
    void shouldRejectBlankVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle(" ", "CAR", "Toyota", "Corolla", 50, 80));
    }

    @Test
    void shouldRejectBlankVehicleType() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle("V1", " ", "Toyota", "Corolla", 50, 80));
    }

    @Test
    void shouldRejectBrandWithComma() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle("V1", "CAR", "Toyota,Inc", "Corolla", 50, 80));
    }

    @Test
    void shouldRejectZeroDailyRate() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle("V1", "CAR", "Toyota", "Corolla", 0, 80));
    }

    @Test
    void shouldRejectNaNDailyRate() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle("V1", "CAR", "Toyota", "Corolla", Double.NaN, 80));
    }

    @Test
    void shouldRejectInfiniteDailyRate() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle("V1", "CAR", "Toyota", "Corolla",
                        Double.POSITIVE_INFINITY, 80));
    }

    @Test
    void shouldRejectEnergyBelowZero() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle("V1", "CAR", "Toyota", "Corolla", 50, -1));
    }

    @Test
    void shouldRejectEnergyAboveOneHundred() {
        assertThrows(IllegalArgumentException.class, () ->
                service().addVehicle("V1", "CAR", "Toyota", "Corolla", 50, 101));
    }

    @Test
    void shouldRejectUnsupportedVehicleType() {
        VehicleRepository repository = mock(VehicleRepository.class);
        when(repository.findById("V1")).thenReturn(Optional.empty());
        VehicleManagementService service = service(repository);

        assertThrows(IllegalArgumentException.class, () ->
                service.addVehicle("V1", "BOAT", "Brand", "Model", 50, 80));
    }

    @Test
    void shouldCreateMotorcycle() {
        assertTrue(addVehicle("M1", "MOTORCYCLE") instanceof Motorcycle);
    }

    @Test
    void shouldCreateVan() {
        assertTrue(addVehicle("V1", "VAN") instanceof Van);
    }

    @Test
    void shouldCreateTruck() {
        assertTrue(addVehicle("T1", "TRUCK") instanceof Truck);
    }

    @Test
    void shouldCreateElectricVehicleUsingLongTypeName() {
        Vehicle vehicle = addVehicle("E1", "ELECTRIC_VEHICLE");

        assertEquals("ELECTRIC", vehicle.getType());
    }

    @Test
    void shouldCreateNormalVehicleAsAvailableAtTwentyPercent() {
        VehicleRepository repository = mock(VehicleRepository.class);
        when(repository.findById("V1")).thenReturn(Optional.empty());

        Vehicle vehicle = service(repository).addVehicle(
                "V1", "CAR", "Toyota", "Corolla", 50, 20
        );

        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
    }

    private Vehicle addVehicle(String id, String type) {
        VehicleRepository repository = mock(VehicleRepository.class);
        when(repository.findById(id)).thenReturn(Optional.empty());
        return service(repository).addVehicle(
                id, type, "Brand", "Model", 50, 80
        );
    }

    private VehicleManagementService service() {
        return service(mock(VehicleRepository.class));
    }

    private VehicleManagementService service(VehicleRepository repository) {
        return new VehicleManagementService(
                repository,
                mock(VehicleFuelRepository.class),
                mock(AuthenticationService.class)
        );
    }
}
