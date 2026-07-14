package com.vehiclerental.service.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationPublisherTest {

    @Test
    void shouldNotifyAllRegisteredObservers() {
        NotificationObserver firstObserver = mock(NotificationObserver.class);
        NotificationObserver secondObserver = mock(NotificationObserver.class);
        NotificationPublisher publisher = new NotificationPublisher();
        publisher.addObserver(firstObserver);
        publisher.addObserver(secondObserver);

        publisher.notifyObservers("manager@test.com", "Test message");

        verify(firstObserver).update("manager@test.com", "Test message");
        verify(secondObserver).update("manager@test.com", "Test message");
        assertEquals(2, publisher.getObserverCount());
    }

    @Test
    void shouldNotAddSameObserverTwice() {
        NotificationObserver observer = mock(NotificationObserver.class);
        NotificationPublisher publisher = new NotificationPublisher();

        publisher.addObserver(observer);
        publisher.addObserver(observer);

        assertEquals(1, publisher.getObserverCount());
    }

    @Test
    void shouldRemoveObserver() {
        NotificationObserver observer = mock(NotificationObserver.class);
        NotificationPublisher publisher = new NotificationPublisher();
        publisher.addObserver(observer);
        publisher.removeObserver(observer);

        publisher.notifyObservers("manager@test.com", "Message");

        verify(observer, never()).update(anyString(), anyString());
        assertEquals(0, publisher.getObserverCount());
    }
}
