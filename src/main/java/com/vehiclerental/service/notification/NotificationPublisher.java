package com.vehiclerental.service.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationPublisher {

    private final List<NotificationObserver> observers = new ArrayList<>();

    public void addObserver(NotificationObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is required.");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String recipient, String message) {
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification recipient is required.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message is required.");
        }

        for (NotificationObserver observer : observers) {
            observer.update(recipient, message);
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}
