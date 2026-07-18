package com.vehiclerental.service.notification;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailNotificationServiceTest {

    @Test
    void shouldPrintNotificationToConsole() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        System.setOut(new PrintStream(output));

        try {
            new EmailNotificationService().sendNotification(
                    "customer@test.com", "Rental reminder"
            );
        } finally {
            System.setOut(originalOutput);
        }

        assertTrue(output.toString().contains("customer@test.com"));
        assertTrue(output.toString().contains("Rental reminder"));
    }
}
