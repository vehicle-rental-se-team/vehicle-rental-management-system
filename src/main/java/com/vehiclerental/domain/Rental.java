package com.vehiclerental.domain;

import java.time.LocalDate;

public class Rental {

    private final String id;
    private final Vehicle vehicle;
    private final String customerName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private boolean active;

    public Rental(String id, Vehicle vehicle, String customerName, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.vehicle = vehicle;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void close() {
        this.active = false;
    }
}
