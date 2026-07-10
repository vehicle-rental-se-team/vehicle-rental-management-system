package com.vehiclerental.service.reminder;

import com.vehiclerental.domain.Rental;
import com.vehiclerental.service.notification.NotificationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalReminderService {

    private final NotificationService notificationService;

    public RentalReminderService(NotificationService notificationService) {
        if (notificationService == null) {
            throw new IllegalArgumentException("Notification service is required.");
        }
        this.notificationService = notificationService;
    }

    public ReminderResult sendExpiryReminders(List<Rental> rentals, LocalDate today) {
        if (rentals == null) {
            throw new IllegalArgumentException("Rentals list is required.");
        }
        if (today == null) {
            throw new IllegalArgumentException("Current date is required.");
        }

        int count = 0;

        for (Rental rental : rentals) {
            if (shouldSendReminder(rental, today)) {
                notificationService.sendReminder(
                        rental.getCustomerEmail(),
                        buildReminderMessage(rental)
                );
                count++;
            }
        }

        return new ReminderResult(count);
    }

    public boolean shouldSendReminder(Rental rental, LocalDate today) {
        if (rental == null || today == null || !rental.isActive()) {
            return false;
        }

        long daysUntilReturn = ChronoUnit.DAYS.between(today, rental.getEndDate());
        return daysUntilReturn == 1;
    }

    private String buildReminderMessage(Rental rental) {
        return "Hello " + rental.getCustomerName()
                + ", your rental for vehicle "
                + rental.getVehicle().getId()
                + " expires on "
                + rental.getEndDate()
                + ".";
    }
}
