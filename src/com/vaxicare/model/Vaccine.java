package com.vaxicare.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Abstract base class representing a vaccine product.
 * Demonstrates Unit II: Abstract classes, inheritance, final methods, 
 * and deliberate private/protected/package-private/public access modifiers.
 */
public abstract class Vaccine implements Schedulable {

    public static final int VIAL_OPEN_HOURS = 6;

    protected String name;
    protected int dosesRequired;
    protected int dosesPerVial;

    // Package-private access modifier
    String batchCode;

    // Private member
    private String lastCalculatedDueDate;

    // Static counter for certificate sequence numbers
    private static int certCounter = 2000;

    public Vaccine(String name, int dosesRequired, int dosesPerVial, String batchCode) {
        this.name = name;
        this.dosesRequired = dosesRequired;
        this.dosesPerVial = dosesPerVial;
        this.batchCode = batchCode;
        this.lastCalculatedDueDate = null;
    }

    /**
     * Abstract method defining the mandatory dose interval in days.
     * Subclasses must provide polymorphic implementation.
     */
    public abstract int intervalDays(int doseNumber);

    /**
     * Checks baseline age eligibility for the vaccine.
     */
    public boolean eligibility(int age) {
        return age >= 0;
    }

    /**
     * Final method to issue standard certificate numbers.
     * Cannot be overridden by any subclass.
     */
    public final String certificateNumber() {
        synchronized (Vaccine.class) {
            certCounter++;
            return "VC-26-" + certCounter;
        }
    }

    @Override
    public String nextDueDate(String lastDose, int doseNo) {
        try {
            LocalDate lastDate = LocalDate.parse(lastDose);
            int interval = intervalDays(doseNo);
            LocalDate dueDate = lastDate.plusDays(interval);
            this.lastCalculatedDueDate = dueDate.toString();
            return this.lastCalculatedDueDate;
        } catch (DateTimeParseException e) {
            LocalDate fallback = LocalDate.now().plusDays(28);
            this.lastCalculatedDueDate = fallback.toString();
            return this.lastCalculatedDueDate;
        }
    }

    @Override
    public boolean isDue(String today) {
        if (lastCalculatedDueDate == null) {
            return true;
        }
        try {
            LocalDate current = LocalDate.parse(today);
            LocalDate due = LocalDate.parse(lastCalculatedDueDate);
            return !current.isBefore(due);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public boolean bookSlot(int sessionId) {
        return sessionId > 0;
    }

    // Accessors and Mutators
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDosesRequired() {
        return dosesRequired;
    }

    public void setDosesRequired(int dosesRequired) {
        this.dosesRequired = dosesRequired;
    }

    public int getDosesPerVial() {
        return dosesPerVial;
    }

    public void setDosesPerVial(int dosesPerVial) {
        this.dosesPerVial = dosesPerVial;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getLastCalculatedDueDate() {
        return lastCalculatedDueDate;
    }

    public void setLastCalculatedDueDate(String lastCalculatedDueDate) {
        this.lastCalculatedDueDate = lastCalculatedDueDate;
    }

    public static synchronized void setCertCounter(int counter) {
        certCounter = counter;
    }

    @Override
    public String toString() {
        return "Vaccine{" +
                "name='" + name + '\'' +
                ", dosesRequired=" + dosesRequired +
                ", dosesPerVial=" + dosesPerVial +
                ", batchCode='" + batchCode + '\'' +
                ", lastCalculatedDueDate='" + lastCalculatedDueDate + '\'' +
                '}';
    }
}
