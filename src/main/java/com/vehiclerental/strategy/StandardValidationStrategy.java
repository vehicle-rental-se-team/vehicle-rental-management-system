package com.vehiclerental.strategy;

import com.vehiclerental.domain.Vehicle;

public class StandardValidationStrategy implements RentalValidationStrategy {
    @Override
    public void validate(Vehicle vehicle, RentalRequest request) {
        if (request.getCustomerAge() < 18) {
            throw new IllegalArgumentException("Customer must be at least 18 years old.");
        }
    }
}
