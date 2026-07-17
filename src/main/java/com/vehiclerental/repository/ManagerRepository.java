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

    private final String filePath;
    private final List<Manager> managers;

    public ManagerRepository() {
        this(MANAGERS_FILE_PATH);
    }

    public ManagerRepository(String filePath) {
        this.filePath = filePath;
        this.managers = new ArrayList<>();
        loadManagersFromFile();
    }

    private void loadManagersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    managers.add(convertLineToManager(line));
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not load managers from file: " + filePath,
                    exception
            );
        }
    }

    private Manager convertLineToManager(String line) {
        String[] parts = line.split(",");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid manager data: " + line);
        }

        return new Manager(parts[0].trim(), parts[1].trim());
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
