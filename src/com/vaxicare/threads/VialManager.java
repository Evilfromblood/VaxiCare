package com.vaxicare.threads;

import com.vaxicare.model.Vaccine;

import java.time.LocalTime;
import java.util.List;
import java.util.Vector;

/**
 * Manages physical vaccine vials, remaining doses, wastage, and shelf-life expiration.
 * Demonstrates Unit III: Method synchronization, race conditions, Vector collection, and toggling locking mechanisms.
 */
public class VialManager {

    private int vialsOpened = 0;
    private int dosesRemainingInCurrentVial = 0;
    private int dosesWasted = 0;
    private long vialOpenedTimeMillis = 0;

    private static final int DOSES_PER_VIAL = 10;
    private static final long EXPIRY_MILLIS = Vaccine.VIAL_OPEN_HOURS * 3600 * 1000L;

    // Vector declared via List interface for thread-safe event logging
    private final List<String> vialLogs = new Vector<>();

    // Toggle flag to contrast safe synchronization vs. race-condition behavior
    private volatile boolean useSynchronization = true;

    public VialManager() {
        vialLogs.add("[" + LocalTime.now() + "] VialManager initialized. Synchronized locking: " + useSynchronization);
    }

    /**
     * Thread-safe synchronized dose drawing.
     * Guarantees atomic check-and-act operations.
     */
    public synchronized boolean drawDoseSynchronized(String threadName) {
        long now = System.currentTimeMillis();

        // 1. Check shelf-life expiry (6 hours)
        if (vialOpenedTimeMillis > 0 && (now - vialOpenedTimeMillis) > EXPIRY_MILLIS) {
            if (dosesRemainingInCurrentVial > 0) {
                dosesWasted += dosesRemainingInCurrentVial;
                vialLogs.add(String.format("[%s] [%s] EXPIRED: Vial discarded after %d hours! %d doses wasted.",
                        LocalTime.now(), threadName, Vaccine.VIAL_OPEN_HOURS, dosesRemainingInCurrentVial));
            }
            dosesRemainingInCurrentVial = 0;
        }

        // 2. Open new vial if current vial is exhausted
        if (dosesRemainingInCurrentVial <= 0) {
            vialsOpened++;
            dosesRemainingInCurrentVial = DOSES_PER_VIAL;
            vialOpenedTimeMillis = now;
            vialLogs.add(String.format("[%s] [%s] Opened new vial #%d (Total opened: %d, doses: %d).",
                    LocalTime.now(), threadName, vialsOpened, vialsOpened, DOSES_PER_VIAL));
        }

        // 3. Atomically consume 1 dose
        dosesRemainingInCurrentVial--;
        vialLogs.add(String.format("[%s] [%s] [SYNC] Drew dose. Remaining in vial #%d: %d doses (Wasted: %d).",
                LocalTime.now(), threadName, vialsOpened, dosesRemainingInCurrentVial, dosesWasted));

        return true;
    }

    /**
     * Unsynchronized dose drawing with deliberate delay.
     * Demonstrates race conditions, lost updates, and corrupted counts under concurrent access.
     */
    public boolean drawDoseUnsynchronized(String threadName) {
        long now = System.currentTimeMillis();

        // Check expiry without lock
        if (vialOpenedTimeMillis > 0 && (now - vialOpenedTimeMillis) > EXPIRY_MILLIS) {
            dosesWasted += dosesRemainingInCurrentVial;
            dosesRemainingInCurrentVial = 0;
        }

        // Read state
        int currentRemaining = dosesRemainingInCurrentVial;

        // Deliberate context-switch window to provoke race condition
        try {
            Thread.sleep(15);
        } catch (InterruptedException ignored) {
        }

        if (currentRemaining <= 0) {
            int newCount = vialsOpened + 1;
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            vialsOpened = newCount;
            dosesRemainingInCurrentVial = DOSES_PER_VIAL;
            vialOpenedTimeMillis = now;
            vialLogs.add(String.format("[%s] [%s] [UNSYNC-RACE] Opened vial #%d.", LocalTime.now(), threadName, vialsOpened));
        }

        // Unsafe update
        int updated = dosesRemainingInCurrentVial - 1;
        dosesRemainingInCurrentVial = updated;

        vialLogs.add(String.format("[%s] [%s] [UNSYNC-RACE] Drew dose. Remaining: %d (Vials opened: %d).",
                LocalTime.now(), threadName, dosesRemainingInCurrentVial, vialsOpened));

        return true;
    }

    /**
     * Dynamic dispatcher based on toggle flag.
     */
    public boolean drawDose(String threadName) {
        if (useSynchronization) {
            return drawDoseSynchronized(threadName);
        } else {
            return drawDoseUnsynchronized(threadName);
        }
    }

    // Accessors and Mutators
    public boolean isUseSynchronization() {
        return useSynchronization;
    }

    public void setUseSynchronization(boolean useSynchronization) {
        this.useSynchronization = useSynchronization;
        vialLogs.add("[" + LocalTime.now() + "] Concurrency mode changed: Synchronized = " + useSynchronization);
    }

    public synchronized int getVialsOpened() {
        return vialsOpened;
    }

    public synchronized int getDosesRemainingInCurrentVial() {
        return dosesRemainingInCurrentVial;
    }

    public synchronized int getDosesWasted() {
        return dosesWasted;
    }

    public List<String> getVialLogs() {
        return vialLogs;
    }
}
