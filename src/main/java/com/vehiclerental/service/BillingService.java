package com.vehiclerental.service;

import com.vehiclerental.domain.Rental;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillingService {

    private final double latePenaltyPerDay;

    public BillingService(double latePenaltyPerDay) {
        this.latePenaltyPerDay = latePenaltyPerDay;
    }

    public double calculateBaseCost(Rental rental) {
        long days = ChronoUnit.DAYS.between(rental.getStartDate(), rental.getEndDate());
        if (days <= 0) {
            days = 1;
        }
        return days * rental.getVehicle().getDailyRate();
    }

    public long calculateLateDays(Rental rental, LocalDate actualReturnDate) {
        if (actualReturnDate == null || !actualReturnDate.isAfter(rental.getEndDate())) {
            return 0;
        }
        return ChronoUnit.DAYS.between(rental.getEndDate(), actualReturnDate);
    }

    public double calculateLatePenalty(Rental rental, LocalDate actualReturnDate) {
        return calculateLateDays(rental, actualReturnDate) * latePenaltyPerDay;
    }

    public double calculateTotalCost(Rental rental, LocalDate actualReturnDate) {
        return calculateBaseCost(rental) + calculateLatePenalty(rental, actualReturnDate);
    }
}
