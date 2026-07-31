package com.vehiclerental.service.notification;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailNotificationServiceTest {

    @Test
    void shouldLogNotification() {
        Logger logger = Logger.getLogger(
                EmailNotificationService.class.getName()
        );

        List<String> messages = new ArrayList<>();

        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {
                // No resources need to be flushed.
            }

            @Override
            public void close() {
                // No resources need to be closed.
            }
        };

        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        try {
            new EmailNotificationService().sendNotification(
                    "customer@test.com",
                    "Rental reminder"
            );
        } finally {
            logger.removeHandler(handler);
        }

        assertTrue(messages.stream()
                .anyMatch(message ->
                        message.contains("customer@test.com")
                                && message.contains("Rental reminder")
                ));
    }
}