package com.vaxicare.model;

import java.util.Objects;

/**
 * Model class representing a vaccination center session.
 */
public class Session {

    private int sessionId;
    private String sessionDate;
    private String vaccineName;
    private int capacity;
    private int booked;

    public Session() {
    }

    public Session(int sessionId, String sessionDate, String vaccineName, int capacity, int booked) {
        this.sessionId = sessionId;
        this.sessionDate = sessionDate;
        this.vaccineName = vaccineName;
        this.capacity = capacity;
        this.booked = booked;
    }

    public Session(String sessionDate, String vaccineName, int capacity) {
        this(0, sessionDate, vaccineName, capacity, 0);
    }

    public boolean isFull() {
        return booked >= capacity;
    }

    public int getAvailableSlots() {
        int diff = capacity - booked;
        return Math.max(diff, 0);
    }

    public void incrementBooked() {
        this.booked++;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getBooked() {
        return booked;
    }

    public void setBooked(int booked) {
        this.booked = booked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return sessionId == session.sessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "Session #" + sessionId + " | " + sessionDate + " | " + vaccineName +
                " | Slots: " + booked + "/" + capacity + " (" + getAvailableSlots() + " left)";
    }
}
