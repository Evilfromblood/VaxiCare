package com.vaxicare.model;

/**
 * Precautionary booster dose vaccine.
 * Declared final to prevent inheritance.
 * Extends AdultVaccine, adds primarySeriesCompleted check, and overrides intervalDays to 180 days.
 */
public final class BoosterVaccine extends AdultVaccine {

    private boolean primarySeriesCompleted;

    public BoosterVaccine(String name, int dosesRequired, int dosesPerVial, String batchCode, boolean primarySeriesCompleted) {
        super(name, dosesRequired, dosesPerVial, batchCode);
        this.primarySeriesCompleted = primarySeriesCompleted;
    }

    public BoosterVaccine(String name, String batchCode, boolean primarySeriesCompleted) {
        super(name, 1, 10, batchCode);
        this.primarySeriesCompleted = primarySeriesCompleted;
    }

    @Override
    public int intervalDays(int doseNumber) {
        return 180;
    }

    @Override
    public boolean eligibility(int age) {
        // Enforce both adult age limit and mandatory completion of primary 2-dose series
        return super.eligibility(age) && primarySeriesCompleted;
    }

    public boolean isPrimarySeriesCompleted() {
        return primarySeriesCompleted;
    }

    public void setPrimarySeriesCompleted(boolean primarySeriesCompleted) {
        this.primarySeriesCompleted = primarySeriesCompleted;
    }
}
