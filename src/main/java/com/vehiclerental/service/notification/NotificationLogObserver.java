package com.vehiclerental.service.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationLogObserver implements NotificationObserver {

    private final List<String> messages = new ArrayList<>();

    @Override
    public void update(String recipient, String message) {
        messages.add("To: " + recipient + "\n" + message);
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clear() {
        messages.clear();
    }
}
