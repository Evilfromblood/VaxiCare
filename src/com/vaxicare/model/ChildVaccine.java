package com.vaxicare.model;

/**
 * Pediatric vaccine implementation.
 * Overrides intervalDays (Dose 2: 28d, Dose 3: 42d) and eligibility (age <= 17).
 */
public class ChildVaccine extends Vaccine {

    public ChildVaccine(String name, int dosesRequired, int dosesPerVial, String batchCode) {
        super(name, dosesRequired, dosesPerVial, batchCode);
    }

    public ChildVaccine(String name, String batchCode) {
        super(name, 3, 10, batchCode);
    }

    @Override
    public int intervalDays(int doseNumber) {
        if (doseNumber == 2) {
            return 28;
        } else if (doseNumber >= 3) {
            return 42;
        }
        return 28;
    }

    @Override
    public boolean eligibility(int age) {
        return age >= 0 && age <= 17;
    }
}
