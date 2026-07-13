package com.vehiclerental.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Rental {

    private final String id;
    private final Vehicle vehicle;
    private final Customer customer;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private boolean active;

    public Rental(String id, Vehicle vehicle, Customer customer, LocalDate startDate, LocalDate endDate) {
        this(id, vehicle, customer, startDate, endDate, true);
    }

    public Rental(String id,
                  Vehicle vehicle,
                  Customer customer,
                  LocalDate startDate,
                  LocalDate endDate,
                  boolean active) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Rental id is required.");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Rental dates are required.");
        }

        this.id = id;
        this.vehicle = vehicle;
        this.customer = customer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getCustomerName() {
        return customer.getName();
    }

    public String getCustomerEmail() {
        return customer.getEmail();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public long getRentalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isReturned() {
        return !active;
    }

    public void close() {
        this.active = false;
    }
}
