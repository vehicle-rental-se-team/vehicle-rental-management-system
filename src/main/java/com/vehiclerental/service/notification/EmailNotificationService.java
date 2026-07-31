package com.vehiclerental.service.notification;

import java.util.logging.Logger;

public class EmailNotificationService implements NotificationService {

    private static final Logger LOGGER =
            Logger.getLogger(EmailNotificationService.class.getName());

    @Override
    public void sendNotification(String recipient, String message) {
        LOGGER.info(() ->
                "Notification sent to " + recipient + ": " + message
        );
    }
}