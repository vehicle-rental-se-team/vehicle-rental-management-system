package com.vehiclerental.service;

import com.vehiclerental.domain.Manager;
import com.vehiclerental.exception.UnauthorizedAccessException;
import com.vehiclerental.repository.ManagerRepository;

import java.util.Optional;

public class AuthenticationService {

    private final ManagerRepository managerRepository;
    private Manager loggedInManager;

    public AuthenticationService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public boolean login(String username, String password) {
        Optional<Manager> managerOptional = managerRepository.findByUsername(username);

        if (!managerOptional.isPresent()) {
            return false;
        }

        Manager manager = managerOptional.get();

        if (manager.getPassword().equals(password)) {
            loggedInManager = manager;
            return true;
        }

        return false;
    }

    public void logout() {
        loggedInManager = null;
    }

    public boolean isLoggedIn() {
        return loggedInManager != null;
    }

    public Manager getLoggedInManager() {
        return loggedInManager;
    }

    public void requireLogin() {
        if (!isLoggedIn()) {
            throw new UnauthorizedAccessException("You must login before performing this action.");
        }
    }
}
