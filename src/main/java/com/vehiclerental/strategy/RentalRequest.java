package com.vehiclerental.strategy;

public class RentalRequest {
    private final int customerAge;
    private final boolean hasSpecialLicense;

    public RentalRequest(int customerAge, boolean hasSpecialLicense) {
        this.customerAge = customerAge;
        this.hasSpecialLicense = hasSpecialLicense;
    }

    public int getCustomerAge() { return customerAge; }
    public boolean hasSpecialLicense() { return hasSpecialLicense; }
}
