package com.vaxicare.model;

import java.util.Objects;

/**
 * Model class representing a vaccination candidate / citizen beneficiary.
 * Demonstrates Unit I: Constructor overloading, this(...) constructor chaining,
 * static variables and utility methods, and Unit II: toString(), equals(), hashCode() overrides.
 */
public class Beneficiary {

    private static int idCounter = 5500;
    public static final String[] AGE_GROUPS = {"Child (0-17)", "Adult (18-59)", "Senior Citizen (60+)"};

    private String benId;
    private String name;
    private int age;
    private String idNumber;
    private String phone;
    private String ageGroup;

    /**
     * Constructor 1: Single parameter name, defaults age to 18.
     * Chained to Constructor 2 via this(...).
     */
    public Beneficiary(String name) {
        this(name, 18);
    }

    /**
     * Constructor 2: Two parameters (name, age), auto-generates default identity and phone.
     * Chained to Constructor 3 via this(...).
     */
    public Beneficiary(String name, int age) {
        this(name, age, "NAT-" + (idCounter + 1), "98" + String.format("%08d", Math.abs((name + age).hashCode()) % 100000000L));
    }

    /**
     * Constructor 3: Four parameters, full initialization with generated benId.
     */
    public Beneficiary(String name, int age, String idNumber, String phone) {
        synchronized (Beneficiary.class) {
            idCounter++;
            this.benId = "B" + idCounter;
        }
        this.name = name;
        this.age = age;
        this.idNumber = idNumber;
        this.phone = phone;
        this.ageGroup = determineAgeGroup(age);
    }

    /**
     * Full constructor for database persistence and entity hydration.
     */
    public Beneficiary(String benId, String name, int age, String idNumber, String phone, String ageGroup) {
        this.benId = benId;
        this.name = name;
        this.age = age;
        this.idNumber = idNumber;
        this.phone = phone;
        this.ageGroup = (ageGroup != null && !ageGroup.isEmpty()) ? ageGroup : determineAgeGroup(age);
        
        // Synchronize idCounter if an imported ID has higher numerical value
        try {
            if (benId != null && benId.startsWith("B")) {
                int numeric = Integer.parseInt(benId.substring(1));
                synchronized (Beneficiary.class) {
                    if (numeric > idCounter) {
                        idCounter = numeric;
                    }
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    /**
     * Static utility validation method for 10-digit telephone numbers.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.trim().matches("^[0-9]{10}$");
    }

    /**
     * Static utility classification method for age demographics.
     */
    public static String determineAgeGroup(int age) {
        if (age <= 17) {
            return AGE_GROUPS[0]; // Child (0-17)
        } else if (age <= 59) {
            return AGE_GROUPS[1]; // Adult (18-59)
        } else {
            return AGE_GROUPS[2]; // Senior Citizen (60+)
        }
    }

    // Getters and Setters
    public String getBenId() {
        return benId;
    }

    public void setBenId(String benId) {
        this.benId = benId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        this.ageGroup = determineAgeGroup(age);
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public static void setIdCounter(int counter) {
        idCounter = counter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Beneficiary that = (Beneficiary) o;
        return Objects.equals(benId, that.benId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(benId);
    }

    @Override
    public String toString() {
        return "[" + benId + "] " + name + " (" + age + "y, " + ageGroup + ") - Ph: " + phone;
    }
}
