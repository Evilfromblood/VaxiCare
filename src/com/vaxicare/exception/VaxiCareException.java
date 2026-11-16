package com.vaxicare.exception;

/**
 * Base checked exception for the VaxiCare vaccination system.
 * Demonstrates Unit III: Exception Handling hierarchy.
 */
public class VaxiCareException extends Exception {

    public VaxiCareException() {
        super("A VaxiCare domain exception occurred.");
    }

    public VaxiCareException(String message) {
        super(message);
    }

    public VaxiCareException(String message, Throwable cause) {
        super(message, cause);
    }
}
