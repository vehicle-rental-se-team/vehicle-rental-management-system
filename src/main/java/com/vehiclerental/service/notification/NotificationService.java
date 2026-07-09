package com.vehiclerental.service.notification;

public interface NotificationService {
    void sendReminder(String customerEmail, String message);
}
