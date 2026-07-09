package com.vehiclerental.service.reminder;

public class ReminderResult {
    private final int remindersSent;

    public ReminderResult(int remindersSent) {
        this.remindersSent = remindersSent;
    }

    public int getRemindersSent() {
        return remindersSent;
    }

    public boolean hasReminders() {
        return remindersSent > 0;
    }

    public String getMessage() {
        if (remindersSent == 0) {
            return "No expiring rentals found.";
        }
        if (remindersSent == 1) {
            return "1 reminder sent.";
        }
        return remindersSent + " reminders sent.";
    }
}
