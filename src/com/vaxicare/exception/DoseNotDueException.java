package com.vaxicare.exception;

/**
 * Thrown when the required interval between vaccination doses has not elapsed.
 * Demonstrates custom exception with contextual fields.
 */
public class DoseNotDueException extends VaxiCareException {

    private final int daysRemaining;
    private final String nextDueDate;

    public DoseNotDueException(String message) {
        super(message);
        this.daysRemaining = -1;
        this.nextDueDate = "N/A";
    }

    public DoseNotDueException(String message, int daysRemaining, String nextDueDate) {
        super(message + " [Days Remaining: " + daysRemaining + ", Next Due Date: " + nextDueDate + "]");
        this.daysRemaining = daysRemaining;
        this.nextDueDate = nextDueDate;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }
}
