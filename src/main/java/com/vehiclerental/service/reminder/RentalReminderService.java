package com.vehiclerental.service.reminder;

import com.vehiclerental.domain.Rental;
import com.vehiclerental.service.notification.NotificationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalReminderService {
    private final NotificationService notificationService;

    public RentalReminderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public ReminderResult sendExpiryReminders(List<Rental> rentals, LocalDate today) {
        int count = 0;
        for (Rental rental : rentals) {
            if (shouldSendReminder(rental, today)) {
                notificationService.sendReminder(rental.getCustomerEmail(), buildReminderMessage(rental));
                count++;
            }
        }
        return new ReminderResult(count);
    }

    public boolean shouldSendReminder(Rental rental, LocalDate today) {
        if (rental == null || today == null || !rental.isActive()) {
            return false;
        }
        long daysUntilReturn = ChronoUnit.DAYS.between(today, rental.getReturnDate());
        return daysUntilReturn == 1;
    }

    private String buildReminderMessage(Rental rental) {
        return "Your rental for vehicle " + rental.getVehicleId() + " expires on " + rental.getReturnDate() + ".";
    }
}
