package com.vehiclerental.service;

import com.vehiclerental.domain.Manager;
import com.vehiclerental.exception.UnauthorizedAccessException;
import com.vehiclerental.repository.ManagerRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Test
    void shouldLoginWithValidCredentials() {
        ManagerRepository repository = mock(ManagerRepository.class);
        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(new Manager("admin", "1234")));

        AuthenticationService service = new AuthenticationService(repository);

        boolean result = service.login("admin", "1234");

        assertTrue(result);
        assertTrue(service.isLoggedIn());
        assertEquals("admin", service.getLoggedInManager().getUsername());
    }

    @Test
    void shouldRejectInvalidPasswordAndClearOldLogin() {
        ManagerRepository repository = mock(ManagerRepository.class);
        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(new Manager("admin", "1234")));

        AuthenticationService service = new AuthenticationService(repository);
        service.login("admin", "1234");

        boolean result = service.login("admin", "wrong");

        assertFalse(result);
        assertFalse(service.isLoggedIn());
    }

    @Test
    void shouldRequireLoginBeforeProtectedAction() {
        ManagerRepository repository = mock(ManagerRepository.class);
        AuthenticationService service = new AuthenticationService(repository);

        assertThrows(UnauthorizedAccessException.class, service::requireLogin);
    }
}
