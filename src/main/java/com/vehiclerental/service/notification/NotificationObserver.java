package com.vehiclerental.service.notification;

public interface NotificationObserver {
    void update(String recipient, String message);
}
