package com.vehiclerental.repository;

import com.vehiclerental.domain.Rental;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RentalRepository {

    private final List<Rental> rentals;

    public RentalRepository() {
        this.rentals = new ArrayList<>();
    }

    public void save(Rental rental) {
        rentals.add(rental);
    }

    public Optional<Rental> findById(String id) {
        for (Rental rental : rentals) {
            if (rental.getId().equals(id)) {
                return Optional.of(rental);
            }
        }

        return Optional.empty();
    }

    public Optional<Rental> findActiveRentalByVehicleId(String vehicleId) {
        for (Rental rental : rentals) {
            if (rental.isActive() && rental.getVehicle().getId().equals(vehicleId)) {
                return Optional.of(rental);
            }
        }

        return Optional.empty();
    }

    public List<Rental> findAll() {
        return new ArrayList<>(rentals);
    }
}
