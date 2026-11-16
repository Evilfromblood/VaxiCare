package com.vaxicare.threads;

import com.vaxicare.model.Beneficiary;

import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Worker thread simulating clinical vaccinator operations.
 * Demonstrates Unit III: Extending Thread, Thread Priorities (MAX_PRIORITY, NORM_PRIORITY),
 * Thread Lifecycle States (RUNNABLE, WAITING, TERMINATED), synchronized queue blocks,
 * and wait()/notifyAll() synchronization primitives.
 */
public class VaccinatorThread extends Thread {

    private final LinkedList<Beneficiary> queue;
    private final VialManager vialManager;
    private final Object reminderLock;
    private volatile boolean running = true;
    private Consumer<Beneficiary> onVaccinated;

    public VaccinatorThread(String name, LinkedList<Beneficiary> queue, VialManager vialManager, Object reminderLock, int priority) {
        super(name);
        this.queue = queue;
        this.vialManager = vialManager;
        this.reminderLock = reminderLock;
        setPriority(priority);
    }

    public void setOnVaccinated(Consumer<Beneficiary> onVaccinated) {
        this.onVaccinated = onVaccinated;
    }

    public void terminate() {
        this.running = false;
        synchronized (queue) {
            queue.notifyAll();
        }
        interrupt();
    }

    @Override
    public void run() {
        System.out.println(String.format("[THREAD-START] %s | Priority: %d | Initial State: %s",
                getName(), getPriority(), getState()));

        while (running) {
            Beneficiary target = null;

            // Wait-Notify queue consumption
            synchronized (queue) {
                while (queue.isEmpty() && running) {
                    try {
                        System.out.println(String.format("[%s] Waiting for patients in queue... State: %s",
                                getName(), getState()));
                        queue.wait();
                    } catch (InterruptedException e) {
                        if (!running) {
                            break;
                        }
                    }
                }

                if (!running) {
                    break;
                }

                target = queue.poll();
            }

            if (target != null) {
                System.out.println(String.format("[%s] [RUNNABLE] Processing beneficiary: %s (%s, Age %d) | State: %s",
                        getName(), target.getName(), target.getBenId(), target.getAge(), getState()));

                // Draw dose from physical vial
                vialManager.drawDose(getName());

                // Inter-thread signaling: notify reminder task monitor
                if (reminderLock != null) {
                    synchronized (reminderLock) {
                        reminderLock.notifyAll();
                    }
                }

                // Execute callback
                if (onVaccinated != null) {
                    try {
                        onVaccinated.accept(target);
                    } catch (Exception e) {
                        System.err.println("[" + getName() + "] Error in onVaccinated callback: " + e.getMessage());
                    }
                }

                // Simulate clinical inoculation duration
                try {
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    if (!running) {
                        break;
                    }
                }
            }
        }

        System.out.println(String.format("[THREAD-TERMINATING] %s shutting down. State: %s",
                getName(), getState()));
    }
}
