package com.vehiclerental.exception;

public class RentalAlreadyExistsException extends RuntimeException {

    public RentalAlreadyExistsException(String message) {
        super(message);
    }
}
