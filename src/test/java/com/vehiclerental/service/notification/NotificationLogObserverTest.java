package com.vehiclerental.service.notification;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationLogObserverTest {

    @Test
    void shouldStoreNotification() {
        NotificationLogObserver observer = new NotificationLogObserver();

        observer.update("customer@test.com", "Battery is low");

        assertEquals(1, observer.getMessages().size());
        assertTrue(observer.getMessages().get(0).contains("Battery is low"));
    }

    @Test
    void shouldReturnCopyOfMessages() {
        NotificationLogObserver observer = new NotificationLogObserver();
        observer.update("customer@test.com", "Message");

        List<String> messages = observer.getMessages();
        messages.clear();

        assertEquals(1, observer.getMessages().size());
    }

    @Test
    void shouldClearNotifications() {
        NotificationLogObserver observer = new NotificationLogObserver();
        observer.update("customer@test.com", "Message");

        observer.clear();

        assertTrue(observer.getMessages().isEmpty());
    }
}
