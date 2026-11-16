package com.vaxicare.exception;

/**
 * Thrown when a beneficiary does not satisfy the age criteria for a vaccine,
 * or when a booster dose is requested without having completed the 2 primary doses.
 */
public class IneligibleAgeException extends VaxiCareException {

    private final int age;
    private final String reason;

    public IneligibleAgeException(String message) {
        super(message);
        this.age = -1;
        this.reason = message;
    }

    public IneligibleAgeException(int age, String reason) {
        super("Beneficiary age [" + age + "] is ineligible: " + reason);
        this.age = age;
        this.reason = reason;
    }

    public int getAge() {
        return age;
    }

    public String getReason() {
        return reason;
    }
}
