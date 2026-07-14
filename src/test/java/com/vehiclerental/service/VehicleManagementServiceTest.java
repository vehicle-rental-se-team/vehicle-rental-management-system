package com.vehiclerental.service;

import com.vehiclerental.domain.ElectricVehicle;
import com.vehiclerental.domain.FuelRecord;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleFuelRepository;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleManagementServiceTest {

    @Test
    void shouldAddElectricVehicleAsAvailableWhenBatteryIsEnough() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleFuelRepository fuelRepository = mock(VehicleFuelRepository.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);
        when(vehicleRepository.findById("E1")).thenReturn(Optional.empty());

        VehicleManagementService service = new VehicleManagementService(
                vehicleRepository, fuelRepository, authenticationService);

        Vehicle vehicle = service.addVehicle(
                "e1", "electric", "Tesla", "Model 3", 100, 80);

        assertTrue(vehicle instanceof ElectricVehicle);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        verify(vehicleRepository).addVehicle(vehicle);
        verify(fuelRepository, never()).saveOrUpdate(any(FuelRecord.class));
    }

    @Test
    void shouldAddNormalVehicleAsUnavailableWhenFuelIsLow() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleFuelRepository fuelRepository = mock(VehicleFuelRepository.class);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.empty());

        VehicleManagementService service = new VehicleManagementService(
                vehicleRepository, fuelRepository, mock(AuthenticationService.class));

        Vehicle vehicle = service.addVehicle(
                "V1", "CAR", "Toyota", "Corolla", 50, 10);

        assertEquals(VehicleStatus.UNAVAILABLE, vehicle.getStatus());
        verify(fuelRepository).saveOrUpdate(any(FuelRecord.class));
    }

    @Test
    void shouldRejectDuplicateVehicleId() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        Vehicle existing = mock(Vehicle.class);
        when(vehicleRepository.findById("V1")).thenReturn(Optional.of(existing));

        VehicleManagementService service = new VehicleManagementService(
                vehicleRepository,
                mock(VehicleFuelRepository.class),
                mock(AuthenticationService.class));

        assertThrows(IllegalArgumentException.class, () ->
                service.addVehicle("V1", "CAR", "Toyota", "Corolla", 50, 80));
    }
}
