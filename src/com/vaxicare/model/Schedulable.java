package com.vaxicare.model;

/**
 * Interface defining scheduling contracts for vaccination workflows.
 * Demonstrates Unit II: Interface definition and abstraction.
 */
public interface Schedulable {

    /**
     * Calculates the subsequent due date based on the previous dose date and dose sequence number.
     * @param lastDose Date of previous dose in YYYY-MM-DD format
     * @param doseNo Next dose sequence number
     * @return Calculated due date as String (YYYY-MM-DD)
     */
    String nextDueDate(String lastDose, int doseNo);

    /**
     * Checks whether the vaccine is due on or before the given reference date.
     * @param today Reference date in YYYY-MM-DD format
     * @return true if due or overdue; false otherwise
     */
    boolean isDue(String today);

    /**
     * Validates and registers a slot booking for the specified session ID.
     * @param sessionId Target session ID
     * @return true if successfully reserved, false otherwise
     */
    boolean bookSlot(int sessionId);
}
