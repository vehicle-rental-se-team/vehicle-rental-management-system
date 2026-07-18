package com.vehiclerental.service.reminder;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.service.notification.NotificationPublisher;
import com.vehiclerental.service.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RentalReminderEdgeCaseTest {

    @Test
    void shouldRejectNullPublisher() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RentalReminderService((NotificationPublisher) null)
        );
    }

    @Test
    void shouldRejectNullNotificationService() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RentalReminderService((NotificationService) null)
        );
    }

    @Test
    void shouldRejectNullRentalsList() {
        RentalReminderService service = service();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendExpiryReminders(null, LocalDate.now())
        );
    }

    @Test
    void shouldRejectNullCurrentDate() {
        RentalReminderService service = service();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendOverdueReminders(Collections.emptyList(), null)
        );
    }

    @Test
    void shouldIgnoreNullRental() {
        RentalReminderService service = service();

        assertFalse(service.shouldSendReminder(null, LocalDate.now()));
        assertFalse(service.shouldSendOverdueReminder(null, LocalDate.now()));
    }

    @Test
    void shouldIgnoreNullDate() {
        RentalReminderService service = service();
        Rental rental = rental(LocalDate.of(2026, 7, 10), true);

        assertFalse(service.shouldSendReminder(rental, null));
        assertFalse(service.shouldSendOverdueReminder(rental, null));
    }

    @Test
    void shouldReturnZeroWhenRentalIsNotOverdue() {
        RentalReminderService service = service();
        Rental rental = rental(LocalDate.of(2026, 7, 10), true);

        long days = service.calculateOverdueDays(
                rental, LocalDate.of(2026, 7, 10)
        );

        assertEquals(0, days);
    }

    @Test
    void shouldSendSingularOverdueMessage() {
        NotificationPublisher publisher = mock(NotificationPublisher.class);
        RentalReminderService service = new RentalReminderService(publisher);
        Rental rental = rental(LocalDate.of(2026, 7, 10), true);

        service.sendOverdueReminders(
                Collections.singletonList(rental),
                LocalDate.of(2026, 7, 11)
        );

        verify(publisher).notifyObservers(
                eq("customer@test.com"), contains("1 day")
        );
    }

    @Test
    void shouldCountExpiryAndOverdueNotifications() {
        RentalReminderService service = service();
        Rental expiring = rental(LocalDate.of(2026, 7, 11), true);
        Rental overdue = rental(LocalDate.of(2026, 7, 9), true);

        ReminderResult result = service.sendRentalNotifications(
                Arrays.asList(expiring, overdue),
                LocalDate.of(2026, 7, 10)
        );

        assertEquals(2, result.getRemindersSent());
    }

    private RentalReminderService service() {
        return new RentalReminderService(mock(NotificationPublisher.class));
    }

    private Rental rental(LocalDate endDate, boolean active) {
        return new Rental(
                "R1",
                new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.RENTED),
                new Customer("C1", "Ahmad", "customer@test.com"),
                LocalDate.of(2026, 7, 1),
                endDate,
                active
        );
    }
}
