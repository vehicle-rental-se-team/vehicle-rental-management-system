package com.vehiclerental.service.reminder;

import com.vehiclerental.domain.Rental;
import com.vehiclerental.service.notification.EmailNotificationObserver;
import com.vehiclerental.service.notification.NotificationPublisher;
import com.vehiclerental.service.notification.NotificationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalReminderService {

    private final NotificationPublisher notificationPublisher;

    public RentalReminderService(NotificationService notificationService) {
        this(createPublisher(notificationService));
    }

    public RentalReminderService(NotificationPublisher notificationPublisher) {
        if (notificationPublisher == null) {
            throw new IllegalArgumentException("Notification publisher is required.");
        }
        this.notificationPublisher = notificationPublisher;
    }

    public ReminderResult sendExpiryReminders(List<Rental> rentals, LocalDate today) {
        validateInputs(rentals, today);

        int count = 0;

        for (Rental rental : rentals) {
            if (shouldSendReminder(rental, today)) {
                notificationPublisher.notifyObservers(
                        rental.getCustomerEmail(),
                        buildReminderMessage(rental)
                );
                count++;
            }
        }

        return new ReminderResult(count);
    }

    public ReminderResult sendOverdueReminders(List<Rental> rentals, LocalDate today) {
        validateInputs(rentals, today);

        int count = 0;

        for (Rental rental : rentals) {
            if (shouldSendOverdueReminder(rental, today)) {
                long overdueDays = calculateOverdueDays(rental, today);

                notificationPublisher.notifyObservers(
                        rental.getCustomerEmail(),
                        buildOverdueMessage(rental, overdueDays)
                );
                count++;
            }
        }

        return new ReminderResult(count);
    }

    public ReminderResult sendRentalNotifications(List<Rental> rentals, LocalDate today) {
        validateInputs(rentals, today);

        int expiryCount = sendExpiryReminders(rentals, today).getRemindersSent();
        int overdueCount = sendOverdueReminders(rentals, today).getRemindersSent();

        return new ReminderResult(expiryCount + overdueCount);
    }

    public boolean shouldSendReminder(Rental rental, LocalDate today) {
        if (rental == null || today == null || !rental.isActive()) {
            return false;
        }

        long daysUntilReturn = ChronoUnit.DAYS.between(today, rental.getEndDate());
        return daysUntilReturn == 1;
    }

    public boolean shouldSendOverdueReminder(Rental rental, LocalDate today) {
        if (rental == null || today == null || !rental.isActive()) {
            return false;
        }

        return today.isAfter(rental.getEndDate());
    }

    public long calculateOverdueDays(Rental rental, LocalDate today) {
        if (!shouldSendOverdueReminder(rental, today)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(rental.getEndDate(), today);
    }

    private String buildReminderMessage(Rental rental) {
        return "Hello " + rental.getCustomerName()
                + ", your rental for vehicle "
                + rental.getVehicle().getId()
                + " expires on "
                + rental.getEndDate()
                + ".";
    }

    private String buildOverdueMessage(Rental rental, long overdueDays) {
        String dayWord = overdueDays == 1 ? "day" : "days";

        return "Hello " + rental.getCustomerName()
                + ", your rental for vehicle "
                + rental.getVehicle().getId()
                + " is overdue by "
                + overdueDays
                + " "
                + dayWord
                + ". Please return it as soon as possible.";
    }

    private void validateInputs(List<Rental> rentals, LocalDate today) {
        if (rentals == null) {
            throw new IllegalArgumentException("Rentals list is required.");
        }
        if (today == null) {
            throw new IllegalArgumentException("Current date is required.");
        }
    }

    private static NotificationPublisher createPublisher(NotificationService notificationService) {
        if (notificationService == null) {
            throw new IllegalArgumentException("Notification service is required.");
        }

        NotificationPublisher publisher = new NotificationPublisher();
        publisher.addObserver(new EmailNotificationObserver(notificationService));
        return publisher;
    }
}
