package com.vehiclerental.service.notification;

public class EmailNotificationObserver implements NotificationObserver {

    private final NotificationService notificationService;

    public EmailNotificationObserver(NotificationService notificationService) {
        if (notificationService == null) {
            throw new IllegalArgumentException("Notification service is required.");
        }
        this.notificationService = notificationService;
    }

    @Override
    public void update(String recipient, String message) {
        notificationService.sendNotification(recipient, message);
    }
}
