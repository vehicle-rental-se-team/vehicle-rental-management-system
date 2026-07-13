package com.vehiclerental.strategy;

import com.vehiclerental.domain.Vehicle;

public interface RentalValidationStrategy {
    void validate(Vehicle vehicle, RentalRequest request);
}
