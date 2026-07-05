package com.vehiclerental.repository;

import com.vehiclerental.domain.Manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManagerRepository {

    private static final String MANAGERS_FILE_PATH = "data/managers.txt";

    private final List<Manager> managers;

    public ManagerRepository() {
        this.managers = new ArrayList<>();
        loadManagersFromFile();
    }

    private void loadManagersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MANAGERS_FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Manager manager = convertLineToManager(line);
                    managers.add(manager);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not load managers from file: " + MANAGERS_FILE_PATH, exception);
        }
    }

    private Manager convertLineToManager(String line) {
        String[] parts = line.split(",");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid manager data: " + line);
        }

        String username = parts[0].trim();
        String password = parts[1].trim();

        return new Manager(username, password);
    }

    public Optional<Manager> findByUsername(String username) {
        for (Manager manager : managers) {
            if (manager.getUsername().equals(username)) {
                return Optional.of(manager);
            }
        }

        return Optional.empty();
    }

    public void addManager(Manager manager) {
        managers.add(manager);
    }
}