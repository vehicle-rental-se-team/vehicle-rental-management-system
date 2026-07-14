package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Vehicle;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleCatalogServiceTest {

    @Test
    void shouldReturnOnlyAvailableVehicles() {
        VehicleRepository repository = mock(VehicleRepository.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);

        Vehicle available = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE);
        Vehicle rented = new Car("V2", "Honda", "Civic", 55, VehicleStatus.RENTED);
        when(repository.findAll()).thenReturn(Arrays.asList(available, rented));

        VehicleCatalogService service =
                new VehicleCatalogService(repository, authenticationService);

        List<Vehicle> result = service.getAvailableVehicles();

        assertEquals(1, result.size());
        assertEquals("V1", result.get(0).getId());
        verify(authenticationService).requireLogin();
    }

    @Test
    void shouldReturnAllVehiclesForManager() {
        VehicleRepository repository = mock(VehicleRepository.class);
        AuthenticationService authenticationService = mock(AuthenticationService.class);
        List<Vehicle> vehicles = Arrays.asList(
                new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.AVAILABLE),
                new Car("V2", "Honda", "Civic", 55, VehicleStatus.RENTED)
        );
        when(repository.findAll()).thenReturn(vehicles);

        VehicleCatalogService service =
                new VehicleCatalogService(repository, authenticationService);

        assertEquals(2, service.getAllVehicles().size());
        verify(authenticationService).requireLogin();
    }
}
