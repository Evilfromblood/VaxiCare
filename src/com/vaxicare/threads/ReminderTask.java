package com.vaxicare.threads;

import com.vaxicare.model.Beneficiary;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Supplier;

/**
 * Background task that monitors reminder schedules and scans beneficiaries due for upcoming doses.
 * Demonstrates Unit III: Implementing Runnable, Thread states inspection, and wait()/notifyAll() coordination.
 */
public class ReminderTask implements Runnable {

    private final Object reminderLock;
    private final Supplier<List<Beneficiary>> dueTomorrowSupplier;
    private final List<VaccinatorThread> monitoredThreads;
    private final List<String> reminderLogs = new Vector<>();
    private volatile boolean running = true;

    public ReminderTask(Object reminderLock, Supplier<List<Beneficiary>> dueTomorrowSupplier) {
        this.reminderLock = reminderLock;
        this.dueTomorrowSupplier = dueTomorrowSupplier;
        this.monitoredThreads = new ArrayList<>();
    }

    public void registerMonitoredThread(VaccinatorThread thread) {
        monitoredThreads.add(thread);
    }

    public void terminate() {
        this.running = false;
        synchronized (reminderLock) {
            reminderLock.notifyAll();
        }
    }

    public List<String> getReminderLogs() {
        return reminderLogs;
    }

    @Override
    public void run() {
        String taskName = Thread.currentThread().getName();
        System.out.println("[REMINDER-TASK] Started: " + taskName + " | Initial State: " + Thread.currentThread().getState());

        while (running) {
            synchronized (reminderLock) {
                try {
                    // Wait for 6 seconds or until awakened by a vaccinator dose administration
                    reminderLock.wait(6000);
                } catch (InterruptedException e) {
                    if (!running) {
                        break;
                    }
                }
            }

            if (!running) {
                break;
            }

            // Inspect and log thread states across the multithreaded ecosystem
            StringBuilder threadStatus = new StringBuilder("[THREAD-INSPECTION] ");
            threadStatus.append(taskName).append(": ").append(Thread.currentThread().getState()).append(" | ");
            for (VaccinatorThread vt : monitoredThreads) {
                threadStatus.append(vt.getName()).append(": ").append(vt.getState())
                        .append(" (Priority ").append(vt.getPriority()).append(") | ");
            }
            String statusStr = threadStatus.toString();
            System.out.println(statusStr);
            reminderLogs.add("[" + LocalTime.now() + "] " + statusStr);

            // Scan beneficiaries due tomorrow
            try {
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                List<Beneficiary> dueList = dueTomorrowSupplier.get();
                int dueCount = (dueList != null) ? dueList.size() : 0;
                String alert = String.format("[%s] [REMINDER-SCAN] Tomorrow (%s): %d beneficiaries due for booster/next dose.",
                        LocalTime.now(), tomorrow, dueCount);
                System.out.println(alert);
                reminderLogs.add(alert);

                if (dueList != null) {
                    for (Beneficiary b : dueList) {
                        reminderLogs.add(String.format("  -> SMS Alert queued for %s (%s, Ph: %s)",
                                b.getName(), b.getBenId(), b.getPhone()));
                    }
                }
            } catch (Exception e) {
                System.err.println("[REMINDER-TASK] Error during due date scan: " + e.getMessage());
            }

            // Notify all threads waiting on reminderLock
            synchronized (reminderLock) {
                reminderLock.notifyAll();
            }
        }

        System.out.println("[REMINDER-TASK] Terminating. Final state: " + Thread.currentThread().getState());
    }
}
