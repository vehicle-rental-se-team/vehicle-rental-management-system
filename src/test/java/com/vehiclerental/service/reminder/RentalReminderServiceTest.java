package com.vehiclerental.service.reminder;

import com.vehiclerental.domain.Car;
import com.vehiclerental.domain.Customer;
import com.vehiclerental.domain.Rental;
import com.vehiclerental.domain.VehicleStatus;
import com.vehiclerental.service.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalReminderServiceTest {

    private Rental createRental(LocalDate endDate) {
        Car car = new Car("V1", "Toyota", "Corolla", 50, VehicleStatus.RENTED);
        Customer customer = new Customer("C1", "Ahmad", "ahmad@test.com");
        return new Rental("R1", car, customer, endDate.minusDays(3), endDate);
    }

    @Test
    void shouldSendExpiryReminderOneDayBeforeEndDate() {
        NotificationService notificationService = mock(NotificationService.class);
        RentalReminderService service = new RentalReminderService(notificationService);
        Rental rental = createRental(LocalDate.of(2026, 7, 10));

        ReminderResult result = service.sendExpiryReminders(
                Collections.singletonList(rental),
                LocalDate.of(2026, 7, 9));

        assertEquals(1, result.getRemindersSent());
        verify(notificationService).sendNotification(
                eq("ahmad@test.com"), contains("expires"));
    }

    @Test
    void shouldSendOverdueReminderWithNumberOfDays() {
        NotificationService notificationService = mock(NotificationService.class);
        RentalReminderService service = new RentalReminderService(notificationService);
        Rental rental = createRental(LocalDate.of(2026, 7, 10));

        ReminderResult result = service.sendOverdueReminders(
                Collections.singletonList(rental),
                LocalDate.of(2026, 7, 13));

        assertEquals(1, result.getRemindersSent());
        assertEquals(3, service.calculateOverdueDays(
                rental, LocalDate.of(2026, 7, 13)));
        verify(notificationService).sendNotification(
                eq("ahmad@test.com"), contains("3 days"));
    }

    @Test
    void shouldIgnoreClosedRental() {
        NotificationService notificationService = mock(NotificationService.class);
        RentalReminderService service = new RentalReminderService(notificationService);
        Rental rental = createRental(LocalDate.of(2026, 7, 10));
        rental.close();

        ReminderResult result = service.sendRentalNotifications(
                Collections.singletonList(rental),
                LocalDate.of(2026, 7, 13));

        assertEquals(0, result.getRemindersSent());
        verify(notificationService, never()).sendNotification(anyString(), anyString());
    }
}
