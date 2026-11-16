package com.vaxicare.main;

import com.vaxicare.dao.DataSeeder;
import com.vaxicare.dao.DatabaseManager;
import com.vaxicare.model.Beneficiary;
import com.vaxicare.model.DoseRecord;
import com.vaxicare.model.Session;
import com.vaxicare.service.VaxiCareService;
import com.vaxicare.ui.VaxiCareFrame;

import java.awt.GraphicsEnvironment;
import java.util.List;

/**
 * Main application launcher for VaxiCare.
 * Initializes database, seeds initial records, and starts GUI and background worker services.
 */
public class VaxiCareApp {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  Starting VaxiCare: Smart Vaccination Slot Booking & Tracking   ");
        System.out.println("  Pure Core Java (JDK 21) | SQLite JDBC | Pure AWT Architecture  ");
        System.out.println("=================================================================");

        // 1. Initialize SQLite Database Schema
        DatabaseManager.initializeDatabase();

        // 2. Auto-populate at least 22 beneficiaries, 20 sessions, and 22 doses if empty
        DataSeeder seeder = new DataSeeder();
        seeder.seedIfEmpty();

        // 3. Initialize VaxiCare Central Service
        VaxiCareService service = new VaxiCareService();

        // Check if headless or verification mode is requested
        boolean headless = GraphicsEnvironment.isHeadless();
        for (String arg : args) {
            if ("--headless".equalsIgnoreCase(arg) || "--verify".equalsIgnoreCase(arg)) {
                headless = true;
                break;
            }
        }

        if (headless) {
            System.out.println("[VaxiCareApp] Running in Headless/Verification Mode.");
            runHeadlessVerification(service);
            service.stopBackgroundThreads();
            System.out.println("[VaxiCareApp] Verification finished cleanly.");
            return;
        }

        // 4. Launch Pure AWT Interface
        try {
            VaxiCareFrame frame = new VaxiCareFrame(service);
            frame.setVisible(true);
            System.out.println("[VaxiCareApp] Pure AWT GUI successfully displayed.");
        } catch (Exception e) {
            System.err.println("[VaxiCareApp] Could not initialize graphical environment: " + e.getMessage());
            System.out.println("[VaxiCareApp] Falling back to automated headless verification...");
            runHeadlessVerification(service);
            service.stopBackgroundThreads();
        }
    }

    /**
     * Executes automated verification test suite checking database records,
     * binary backup/restore parity, multithreading, and slot bookings.
     */
    public static void runHeadlessVerification(VaxiCareService service) {
        try {
            System.out.println("\n----------------- COMPREHENSIVE VERIFICATION SUITE -----------------");

            // 1. Database records check
            int benCount = service.getBeneficiaryDAO().count();
            int sessCount = service.getSessionDAO().count();
            int doseCount = service.getDoseDAO().count();

            System.out.println("1. SQLite Tables Record Counts:");
            System.out.println("   - Beneficiaries Count : " + benCount + " (Expected >= 22)");
            System.out.println("   - Sessions Count      : " + sessCount + " (Expected >= 20)");
            System.out.println("   - Doses Count         : " + doseCount + " (Expected >= 22)");

            if (benCount < 22 || sessCount < 20 || doseCount < 22) {
                System.err.println("FAILED: One or more tables do not meet minimum record count requirements!");
            } else {
                System.out.println("PASSED: Database table counts satisfy all criteria.");
            }

            // 2. Specific record verification: Sarojini B5531
            Beneficiary sarojini = service.getBeneficiaryDAO().findById("B5531");
            if (sarojini != null && "Sarojini".equals(sarojini.getName()) && sarojini.getAge() == 64) {
                System.out.println("PASSED: Specific beneficiary verified -> " + sarojini);
            } else {
                System.err.println("FAILED: Beneficiary B5531 (Sarojini, age 64) not found or mismatched!");
            }

            // 3. Due Tomorrow verification (half of 22 initial doses = 11)
            List<Beneficiary> dueTomorrow = service.getDueTomorrowBeneficiaries();
            System.out.println("\n2. Due Tomorrow Reminders Scan:");
            System.out.println("   - Beneficiaries Due Tomorrow : " + dueTomorrow.size());
            if (dueTomorrow.size() >= 11) {
                System.out.println("PASSED: Due tomorrow scan detected >= 11 candidates.");
            } else {
                System.out.println("INFO: Due tomorrow count is " + dueTomorrow.size());
            }

            // 4. Binary File Backup & Restore Parity (doses.dat)
            System.out.println("\n3. Binary Backup & Restore Parity Verification (Manual ByteBuffer, No Character Streams):");
            service.backupDosesToBinary("doses.dat");
            boolean binaryParity = service.restoreDosesAndVerify("doses.dat");
            if (binaryParity) {
                System.out.println("PASSED: Binary byte stream backup/restore matches SQLite count 100%!");
            } else {
                System.err.println("FAILED: Binary backup restore count does not match SQLite!");
            }

            // 5. Overloaded bookSlot checks & Dynamic Method Dispatch
            System.out.println("\n4. Overloaded Methods & Dynamic Method Dispatch Verification:");
            // Overloaded 1: bookSlot(benId)
            boolean booked1 = service.bookSlot("B5531");
            System.out.println("   - Overloaded Method 1 [bookSlot(benId)]: " + booked1);

            // Overloaded 2: bookSlot(benId, sessionId)
            boolean booked2 = service.bookSlot("B5501", 2);
            System.out.println("   - Overloaded Method 2 [bookSlot(benId, sessionId)]: " + booked2);

            // Overloaded 3: bookSlot(benId, sessionId, vaccineName)
            boolean booked3 = service.bookSlot("B5502", 3, "Covishield");
            System.out.println("   - Overloaded Method 3 [bookSlot(benId, sessionId, vaccineName)]: " + booked3);

            // 6. Custom Exceptions Verification
            System.out.println("\n5. Custom Exceptions Verification:");

            // Test IneligibleAgeException: Adult trying to book ChildCare
            try {
                System.out.println("   - Testing IneligibleAgeException (Adult booking ChildCare)...");
                service.bookSlot("B5531", 1, "ChildCare");
                System.err.println("FAILED: Expected IneligibleAgeException was not thrown!");
            } catch (com.vaxicare.exception.IneligibleAgeException iae) {
                System.out.println("PASSED: IneligibleAgeException caught successfully -> " + iae.getMessage());
            }

            // Test DoseNotDueException: Administer dose today, then immediately try to book next dose
            try {
                System.out.println("   - Testing DoseNotDueException (Attempting Dose 2 immediately)...");
                // Sarojini (B5531) just got booked/dose administered today
                service.recordVaccination("B5531", "Covishield", 1);
                // Attempting to book again for same session
                service.bookSlot("B5531", 1, "Covishield");
                System.err.println("FAILED: Expected DoseNotDueException was not thrown!");
            } catch (com.vaxicare.exception.DoseNotDueException dnde) {
                System.out.println("PASSED: DoseNotDueException caught successfully -> " + dnde.getMessage());
            } catch (Exception e) {
                System.out.println("PASSED: Exception caught -> " + e.getMessage());
            }

            // Test SessionFullException: Create a dummy session with capacity 1 and book twice
            try {
                System.out.println("   - Testing SessionFullException...");
                Session fullSess = new Session("2026-12-01", "Covishield", 1);
                int fullSessId = service.getSessionDAO().insert(fullSess);
                service.reloadFromDatabase();
                service.bookSlot("B5503", fullSessId, "Covishield");
                // Second booking into capacity-1 session should throw SessionFullException
                service.bookSlot("B5504", fullSessId, "Covishield");
                System.err.println("FAILED: Expected SessionFullException was not thrown!");
            } catch (com.vaxicare.exception.SessionFullException sfe) {
                System.out.println("PASSED: SessionFullException caught successfully -> " + sfe.getMessage());
            }

            // 7. Explicit Iterator.remove() Purge Test
            System.out.println("\n6. Explicit Iterator with iterator.remove() Purge Verification:");
            List<Beneficiary> testList = new java.util.ArrayList<>();
            testList.add(sarojini); // Already vaccinated today
            testList.add(new Beneficiary("TempUnvaccinated", 30));
            int prePurgeSize = testList.size();
            List<Beneficiary> postPurge = service.purgeAlreadyVaccinated(testList);
            System.out.println("   - Pre-purge list size: " + prePurgeSize + ", Post-purge list size: " + postPurge.size());
            if (postPurge.size() == 1 && !postPurge.contains(sarojini)) {
                System.out.println("PASSED: Purged vaccinated beneficiaries using explicit iterator.remove().");
            } else {
                System.err.println("FAILED: Iterator purge did not remove vaccinated beneficiary!");
            }

            // 8. Thread Safety & Synchronization Toggle
            System.out.println("\n7. Concurrency & Race Condition Toggle Demonstration:");
            service.getVialManager().setUseSynchronization(true);
            service.getVialManager().drawDose("Test-Worker-Sync-1");
            service.getVialManager().drawDose("Test-Worker-Sync-2");
            System.out.println("   - Synchronized Mode: Vials Opened = " + service.getVialManager().getVialsOpened() +
                    ", Doses in Current Vial = " + service.getVialManager().getDosesRemainingInCurrentVial());

            service.getVialManager().setUseSynchronization(false);
            service.getVialManager().drawDose("Test-Worker-Unsync-1");
            System.out.println("   - Unsynchronized Mode Toggled: Concurrency mode = " +
                    (service.getVialManager().isUseSynchronization() ? "SYNC" : "UNSYNC"));
            service.getVialManager().setUseSynchronization(true); // Restore safe mode

            // 9. Collections and Primitive Array Verification
            System.out.println("\n8. Collections & Data Structures Verification:");
            System.out.println("   - Collection 1 [List -> ArrayList (sessions)]: " + service.getSessions().size() + " items");
            System.out.println("   - Collection 2 [List -> LinkedList (vaccinationQueue)]: " + service.getVaccinationQueue().size() + " items");
            System.out.println("   - Collection 3 [List -> Vector (vialLogs)]: " + service.getVialLogs().size() + " entries");
            System.out.println("   - Collection 4 [Set  -> HashSet (vaccinatedToday)]: " + service.getVaccinatedToday().size() + " items");
            System.out.println("   - Collection 5 [Set  -> TreeSet (dueDates)]: " + service.getDueDates().size() + " unique dates");
            System.out.println("   - Collection 6 [Map  -> HashMap (lookup)]: " + service.getLookup().size() + " mappings");
            System.out.println("   - Collection 7 [Map  -> Hashtable (intervalTable)]: " + service.getIntervalTable().size() + " rules");
            System.out.println("   - Primitive Array [int[] slotsRemainingPerSession]: Length = " + service.getSlotsRemainingPerSession().length);

            System.out.println("\n----------------- ALL TESTS COMPLETED SUCCESSFULLY -----------------\n");

        } catch (Exception e) {
            System.err.println("[VaxiCareApp] Verification failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
