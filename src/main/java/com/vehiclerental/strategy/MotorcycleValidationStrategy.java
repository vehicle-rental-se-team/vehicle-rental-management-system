package com.vehiclerental.strategy;

import com.vehiclerental.domain.Vehicle;

public class MotorcycleValidationStrategy implements RentalValidationStrategy {
    private static final int MINIMUM_AGE = 21;

    @Override
    public void validate(Vehicle vehicle, RentalRequest request) {
        if (request.getCustomerAge() < MINIMUM_AGE) {
            throw new IllegalArgumentException("Customer must be at least 21 years old to rent a motorcycle.");
        }
    }
}
