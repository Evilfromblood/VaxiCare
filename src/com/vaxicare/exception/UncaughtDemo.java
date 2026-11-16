package com.vaxicare.exception;

/**
 * Standalone demonstration class that intentionally triggers an uncaught ArithmeticException.
 * Demonstrates Unit III: Java Default Exception Handler and Uncaught Runtime Exceptions.
 */
public class UncaughtDemo {

    public static void main(String[] args) {
        System.out.println("=== VaxiCare: Uncaught Exception Demonstration ===");
        System.out.println("Attempting to calculate vaccine vial distribution...");
        
        int totalVials = 45;
        int activeDistributionCenters = 0; // Deliberate zero divisor

        System.out.println("Dividing " + totalVials + " vials across " + activeDistributionCenters + " centers...");
        
        // This will deliberately trigger java.lang.ArithmeticException: / by zero
        int vialsPerCenter = totalVials / activeDistributionCenters;
        
        // The following line will not be reached:
        System.out.println("Vials allocated per center: " + vialsPerCenter);
    }
}
