package com.vehiclerental.domain;

/**
 * Represents a vehicle that can be rented in the system.
 */
public class Vehicle {

    /**
     * The unique id of the vehicle.
     */
    private final String id;

    /**
     * The vehicle brand.
     */
    private final String brand;

    /**
     * The vehicle model.
     */
    private final String model;

    /**
     * The rental price for one day.
     */
    private final double dailyRate;

    /**
     * The current status of the vehicle.
     */
    private VehicleStatus status;

    /**
     * Creates a vehicle with its basic information.
     *
     * @param id the vehicle id
     * @param brand the vehicle brand
     * @param model the vehicle model
     * @param dailyRate the daily rental price
     * @param status the current vehicle status
     */
    public Vehicle(
            String id,
            String brand,
            String model,
            double dailyRate,
            VehicleStatus status) {

        this.id = id;
        this.brand = brand;
        this.model = model;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    /**
     * Returns the vehicle id.
     *
     * @return the vehicle id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the vehicle brand.
     *
     * @return the vehicle brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Returns the vehicle model.
     *
     * @return the vehicle model
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the price of renting the vehicle for one day.
     *
     * @return the daily rental price
     */
    public double getDailyRate() {
        return dailyRate;
    }

    /**
     * Returns the current vehicle status.
     *
     * @return the current status
     */
    public VehicleStatus getStatus() {
        return status;
    }

    /**
     * Changes the current vehicle status.
     *
     * @param status the new vehicle status
     */
    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    /**
     * Checks whether the vehicle is available for rental.
     *
     * @return true when the vehicle is available, otherwise false
     */
    public boolean isAvailable() {
        return VehicleStatus.AVAILABLE.equals(status);
    }

    /**
     * Returns the type used for a standard vehicle.
     *
     * @return the vehicle type
     */
    public String getType() {
        return "STANDARD";
    }

    /**
     * Returns a text representation of the vehicle.
     *
     * @return the vehicle details as text
     */
    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", type='" + getType() + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", dailyRate=" + dailyRate +
                ", status=" + status +
                '}';
    }
}
