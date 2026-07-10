package com.vehiclerental.service.notification;

public class EmailNotificationService implements NotificationService {
    @Override
    public void sendReminder(String customerEmail, String message) {
        System.out.println("Reminder sent to " + customerEmail + ": " + message);
    }
}
