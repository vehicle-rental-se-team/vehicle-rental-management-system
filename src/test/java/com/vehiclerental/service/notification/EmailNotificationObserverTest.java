package com.vehiclerental.service.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailNotificationObserverTest {

    @Test
    void shouldSendNotificationUsingService() {
        NotificationService service = mock(NotificationService.class);
        EmailNotificationObserver observer = new EmailNotificationObserver(service);

        observer.update("customer@test.com", "Rental reminder");

        verify(service).sendNotification("customer@test.com", "Rental reminder");
    }

    @Test
    void shouldRejectNullNotificationService() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmailNotificationObserver(null)
        );
    }
}
