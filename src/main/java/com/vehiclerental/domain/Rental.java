package com.vehiclerental.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a rental made by a customer for one vehicle.
 */
public class Rental {

    /**
     * The unique id of the rental.
     */
    private final String id;

    /**
     * The vehicle included in the rental.
     */
    private final Vehicle vehicle;

    /**
     * The customer who made the rental.
     */
    private final Customer customer;

    /**
     * The first day of the rental.
     */
    private final LocalDate startDate;

    /**
     * The last day of the rental.
     */
    private final LocalDate endDate;

    /**
     * Shows whether the rental is still active.
     */
    private boolean active;

    /**
     * Creates an active rental.
     *
     * @param id the rental id
     * @param vehicle the rented vehicle
     * @param customer the customer
     * @param startDate the rental start date
     * @param endDate the rental end date
     */
    public Rental(String id, Vehicle vehicle, Customer customer, LocalDate startDate, LocalDate endDate) {
        this(id, vehicle, customer, startDate, endDate, true);
    }

    /**
     * Creates a rental with a selected active status.
     *
     * @param id the rental id
     * @param vehicle the rented vehicle
     * @param customer the customer
     * @param startDate the rental start date
     * @param endDate the rental end date
     * @param active true when the rental is active
     * @throws IllegalArgumentException when required rental data is missing
     */
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

    /**
     * Returns the rental id.
     *
     * @return the rental id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the rented vehicle.
     *
     * @return the rented vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Returns the customer name.
     *
     * @return the customer name
     */
    public String getCustomerName() {
        return customer.getName();
    }

    /**
     * Returns the customer email.
     *
     * @return the customer email
     */
    public String getCustomerEmail() {
        return customer.getEmail();
    }

    /**
     * Returns the rental start date.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Returns the rental end date.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Calculates the number of rental days.
     *
     * @return the number of days between the start and end dates
     */
    public long getRentalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Checks whether the rental is active.
     *
     * @return true when the rental is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks whether the vehicle was returned.
     *
     * @return true when the rental is closed
     */
    public boolean isReturned() {
        return !active;
    }

    /**
     * Closes the rental after the vehicle is returned.
     */
    public void close() {
        this.active = false;
    }
}
