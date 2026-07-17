package com.vehiclerental.repository;

import com.vehiclerental.domain.Manager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagerRepositoryTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldLoadManagerFromFile() throws IOException {
        Path file = createFile("admin,1234");

        ManagerRepository repository = new ManagerRepository(file.toString());

        assertTrue(repository.findByUsername("admin").isPresent());
        assertEquals("1234",
                repository.findByUsername("admin").get().getPassword());
    }

    @Test
    void shouldReturnEmptyForUnknownManager() throws IOException {
        Path file = createFile("admin,1234");
        ManagerRepository repository = new ManagerRepository(file.toString());

        assertFalse(repository.findByUsername("unknown").isPresent());
    }

    @Test
    void shouldAddManagerToMemory() throws IOException {
        Path file = createFile("");
        ManagerRepository repository = new ManagerRepository(file.toString());

        repository.addManager(new Manager("manager", "pass"));

        assertTrue(repository.findByUsername("manager").isPresent());
    }

    @Test
    void shouldRejectInvalidManagerData() throws IOException {
        Path file = createFile("invalid-line");

        assertThrows(IllegalArgumentException.class,
                () -> new ManagerRepository(file.toString()));
    }

    private Path createFile(String line) throws IOException {
        Path file = tempDirectory.resolve("managers.txt");
        Files.write(
                file,
                Arrays.asList(line),
                StandardCharsets.UTF_8
        );
        return file;
    }
}
