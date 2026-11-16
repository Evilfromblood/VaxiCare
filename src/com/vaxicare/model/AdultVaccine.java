package com.vaxicare.model;

/**
 * Standard adult vaccine implementation.
 * Adds protected minimumAge = 18, calls super(...), overrides intervalDays (28d) and eligibility.
 */
public class AdultVaccine extends Vaccine {

    protected int minimumAge = 18;

    public AdultVaccine(String name, int dosesRequired, int dosesPerVial, String batchCode) {
        super(name, dosesRequired, dosesPerVial, batchCode);
    }

    public AdultVaccine(String name, String batchCode) {
        super(name, 2, 10, batchCode);
    }

    @Override
    public int intervalDays(int doseNumber) {
        return 28;
    }

    @Override
    public boolean eligibility(int age) {
        return age >= minimumAge && age <= 125;
    }

    public int getMinimumAge() {
        return minimumAge;
    }

    public void setMinimumAge(int minimumAge) {
        this.minimumAge = minimumAge;
    }
}
