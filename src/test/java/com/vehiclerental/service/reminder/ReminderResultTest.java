package com.vehiclerental.service.reminder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReminderResultTest {

    @Test
    void shouldReportNoReminders() {
        ReminderResult result = new ReminderResult(0);

        assertFalse(result.hasReminders());
        assertEquals("No expiring rentals found.", result.getMessage());
    }

    @Test
    void shouldReportOneReminder() {
        ReminderResult result = new ReminderResult(1);

        assertTrue(result.hasReminders());
        assertEquals("1 reminder sent.", result.getMessage());
    }

    @Test
    void shouldReportMultipleReminders() {
        ReminderResult result = new ReminderResult(3);

        assertEquals(3, result.getRemindersSent());
        assertEquals("3 reminders sent.", result.getMessage());
    }
}
