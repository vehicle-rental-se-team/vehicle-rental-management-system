package com.vehiclerental.service;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.Manager;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.repository.ManagerRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationAndBillingEdgeCaseTest {

    @Test
    void shouldRejectUnknownManager() {
        ManagerRepository repository = mock(ManagerRepository.class);
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());
        AuthenticationService service = new AuthenticationService(repository);

        assertFalse(service.login("unknown", "password"));
        assertNull(service.getLoggedInManager());
    }

    @Test
    void shouldLogoutLoggedInManager() {
        ManagerRepository repository = mock(ManagerRepository.class);
        Manager manager = new Manager("admin", "pass");
        when(repository.findByUsername("admin")).thenReturn(Optional.of(manager));
        AuthenticationService service = new AuthenticationService(repository);
        service.login("admin", "pass");

        service.logout();

        assertFalse(service.isLoggedIn());
        assertNull(service.getLoggedInManager());
    }

    @Test
    void shouldRequireNoExceptionWhenLoggedIn() {
        ManagerRepository repository = mock(ManagerRepository.class);
        Manager manager = new Manager("admin", "pass");
        when(repository.findByUsername("admin")).thenReturn(Optional.of(manager));
        AuthenticationService service = new AuthenticationService(repository);

        assertTrue(service.login("admin", "pass"));
        service.requireLogin();
    }

    @Test
    void shouldChargeOneDayForSameDayRental() {
        Rental rental = rental(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1));

        assertEquals(50.0, new BillingService(20).calculateBaseCost(rental));
    }

    @Test
    void shouldReturnZeroLateDaysForNullReturnDate() {
        Rental rental = rental(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3));

        assertEquals(0, new BillingService(20).calculateLateDays(rental, null));
    }

    @Test
    void shouldReturnZeroLateDaysBeforeEndDate() {
        Rental rental = rental(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3));

        assertEquals(
                0,
                new BillingService(20).calculateLateDays(
                        rental, LocalDate.of(2026, 7, 2)
                )
        );
    }

    private Rental rental(LocalDate startDate, LocalDate endDate) {
        return new Rental(
                "R1",
                new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.RENTED),
                new Customer("C1", "Ahmad", "a@test.com"),
                startDate,
                endDate
        );
    }
}
