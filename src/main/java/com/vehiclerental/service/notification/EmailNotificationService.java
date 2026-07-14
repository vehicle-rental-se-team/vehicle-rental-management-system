package com.vehiclerental.service.notification;

public class EmailNotificationService implements NotificationService {

    @Override
    public void sendNotification(String recipient, String message) {
        System.out.println("Notification sent to " + recipient + ": " + message);
    }
}
