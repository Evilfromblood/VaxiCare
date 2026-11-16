package com.vaxicare.exception;

/**
 * Thrown when attempting to book a slot in a session that has reached its maximum capacity.
 */
public class SessionFullException extends VaxiCareException {

    private final int sessionId;
    private final int capacity;

    public SessionFullException(String message) {
        super(message);
        this.sessionId = -1;
        this.capacity = -1;
    }

    public SessionFullException(int sessionId, int capacity) {
        super("Vaccination Session #" + sessionId + " is fully booked! (Capacity: " + capacity + ")");
        this.sessionId = sessionId;
        this.capacity = capacity;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getCapacity() {
        return capacity;
    }
}
