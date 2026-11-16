package com.vaxicare.service;

import com.vaxicare.dao.BeneficiaryDAO;
import com.vaxicare.dao.DoseDAO;
import com.vaxicare.dao.SessionDAO;
import com.vaxicare.exception.DoseNotDueException;
import com.vaxicare.exception.IneligibleAgeException;
import com.vaxicare.exception.SessionFullException;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.io.BinaryBackupManager;
import com.vaxicare.model.AdultVaccine;
import com.vaxicare.model.Beneficiary;
import com.vaxicare.model.BoosterVaccine;
import com.vaxicare.model.ChildVaccine;
import com.vaxicare.model.DoseRecord;
import com.vaxicare.model.Session;
import com.vaxicare.model.Vaccine;
import com.vaxicare.threads.ReminderTask;
import com.vaxicare.threads.VaccinatorThread;
import com.vaxicare.threads.VialManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Central business logic and orchestration service for VaxiCare.
 * Demonstrates Unit IV: Collections framework (7 required interfaces and implementations),
 * Unit I: Primitive arrays, method overloading, Unit II: Dynamic method dispatch,
 * and explicit Iterator usage with iterator.remove().
 */
public class VaxiCareService {

    // 1. List interface -> ArrayList
    private final List<Session> sessions = new ArrayList<>();

    // 2. List interface -> LinkedList (queue for vaccination)
    private final List<Beneficiary> vaccinationQueue = new LinkedList<>();

    // 3. List interface -> Vector (thread-safe vial event logs)
    private final List<String> vialLogs = new Vector<>();

    // 4. Set interface -> HashSet (vaccinated today tracking)
    private final Set<String> vaccinatedToday = new HashSet<>();

    // 5. Set interface -> TreeSet (sorted chronological due dates)
    private final Set<String> dueDates = new TreeSet<>();

    // 6. Map interface -> HashMap (O(1) beneficiary ID lookup)
    private final Map<String, Beneficiary> lookup = new HashMap<>();

    // 7. Map interface -> Hashtable (synchronized vaccine dose interval table)
    private final Map<String, Integer> intervalTable = new Hashtable<>();

    // Required Primitive Array tracking slots remaining per session
    private int[] slotsRemainingPerSession = new int[0];

    // DAOs and Subsystems
    private final BeneficiaryDAO beneficiaryDAO;
    private final SessionDAO sessionDAO;
    private final DoseDAO doseDAO;
    private final VialManager vialManager;
    private final BinaryBackupManager backupManager;

    // Multithreading components
    private final Object reminderLock = new Object();
    private VaccinatorThread seniorVaccinator;
    private VaccinatorThread generalVaccinator;
    private ReminderTask reminderTask;
    private Thread reminderThread;

    public VaxiCareService() {
        this.beneficiaryDAO = new BeneficiaryDAO();
        this.sessionDAO = new SessionDAO();
        this.doseDAO = new DoseDAO();
        this.vialManager = new VialManager();
        this.backupManager = new BinaryBackupManager();

        // Initialize default interval table
        intervalTable.put("ChildCare", 28);
        intervalTable.put("Covishield", 28);
        intervalTable.put("Covaxin", 28);
        intervalTable.put("BoosterCare", 180);

        reloadFromDatabase();
        initThreads();
    }

    /**
     * Synchronizes memory collections and primitive array with SQLite database.
     */
    public synchronized void reloadFromDatabase() {
        try {
            // Load beneficiaries into lookup
            lookup.clear();
            List<Beneficiary> bList = beneficiaryDAO.findAll();
            for (Beneficiary b : bList) {
                lookup.put(b.getBenId(), b);
            }

            // Load sessions and initialize slotsRemainingPerSession primitive array
            sessions.clear();
            List<Session> sList = sessionDAO.findAll();
            sessions.addAll(sList);
            slotsRemainingPerSession = new int[sessions.size()];
            for (int i = 0; i < sessions.size(); i++) {
                slotsRemainingPerSession[i] = sessions.get(i).getAvailableSlots();
            }

            // Load dose records to populate dueDates and vaccinatedToday
            List<DoseRecord> dList = doseDAO.findAll();
            dueDates.clear();
            String todayStr = LocalDate.now().toString();
            for (DoseRecord d : dList) {
                if (d.getNextDue() != null && !d.getNextDue().isEmpty()) {
                    dueDates.add(d.getNextDue());
                }
                if (todayStr.equals(d.getDoseDate())) {
                    vaccinatedToday.add(d.getBenId());
                }
            }

            System.out.println(String.format("[VaxiCareService] Loaded: %d beneficiaries, %d sessions, %d doses.",
                    lookup.size(), sessions.size(), dList.size()));

        } catch (SQLException e) {
            System.err.println("[VaxiCareService] Error reloading from database: " + e.getMessage());
        }
    }

    private void initThreads() {
        // High priority vaccinator for seniors (Thread.MAX_PRIORITY = 10)
        seniorVaccinator = new VaccinatorThread("Vaccinator-Senior-Priority",
                (LinkedList<Beneficiary>) vaccinationQueue, vialManager, reminderLock, Thread.MAX_PRIORITY);

        // Normal priority vaccinator for general population (Thread.NORM_PRIORITY = 5)
        generalVaccinator = new VaccinatorThread("Vaccinator-General",
                (LinkedList<Beneficiary>) vaccinationQueue, vialManager, reminderLock, Thread.NORM_PRIORITY);

        // Reminder background task
        reminderTask = new ReminderTask(reminderLock, this::getDueTomorrowBeneficiaries);
        reminderTask.registerMonitoredThread(seniorVaccinator);
        reminderTask.registerMonitoredThread(generalVaccinator);

        reminderThread = new Thread(reminderTask, "ReminderScanService");
        reminderThread.setDaemon(true);
    }

    public void startBackgroundThreads() {
        if (!seniorVaccinator.isAlive()) {
            seniorVaccinator.start();
        }
        if (!generalVaccinator.isAlive()) {
            generalVaccinator.start();
        }
        if (!reminderThread.isAlive()) {
            reminderThread.start();
        }
    }

    public void stopBackgroundThreads() {
        if (seniorVaccinator != null) seniorVaccinator.terminate();
        if (generalVaccinator != null) generalVaccinator.terminate();
        if (reminderTask != null) reminderTask.terminate();
    }

    // ==========================================
    // 3 OVERLOADED bookSlot METHODS
    // ==========================================

    /**
     * Overloaded bookSlot 1: Automatically matches the beneficiary to the first available open session.
     */
    public synchronized boolean bookSlot(String beneficiaryId) throws VaxiCareException, SQLException {
        if (sessions.isEmpty()) {
            throw new VaxiCareException("No vaccination sessions are currently configured in the system.");
        }
        Session target = null;
        for (Session s : sessions) {
            if (s.getAvailableSlots() > 0) {
                target = s;
                break;
            }
        }
        if (target == null) {
            throw new SessionFullException("All available vaccination sessions are completely full.");
        }
        return bookSlot(beneficiaryId, target.getSessionId(), target.getVaccineName());
    }

    /**
     * Overloaded bookSlot 2: Reserves a slot for a specific session ID.
     */
    public synchronized boolean bookSlot(String beneficiaryId, int sessionId) throws VaxiCareException, SQLException {
        Session target = null;
        for (Session s : sessions) {
            if (s.getSessionId() == sessionId) {
                target = s;
                break;
            }
        }
        if (target == null) {
            Session fromDb = sessionDAO.findById(sessionId);
            if (fromDb == null) {
                throw new VaxiCareException("Session #" + sessionId + " does not exist.");
            }
            target = fromDb;
        }
        return bookSlot(beneficiaryId, sessionId, target.getVaccineName());
    }

    /**
     * Overloaded bookSlot 3: Full specification with beneficiary ID, session ID, and vaccine brand.
     * Enforces clinical eligibility and dose intervals using Dynamic Method Dispatch.
     */
    public synchronized boolean bookSlot(String beneficiaryId, int sessionId, String vaccineName)
            throws VaxiCareException, SQLException {

        // 1. Beneficiary Validation
        Beneficiary ben = lookup.get(beneficiaryId);
        if (ben == null) {
            ben = beneficiaryDAO.findById(beneficiaryId);
            if (ben == null) {
                throw new VaxiCareException("Beneficiary with ID '" + beneficiaryId + "' not registered.");
            }
            lookup.put(beneficiaryId, ben);
        }

        // 2. Session Validation and Capacity Check
        Session targetSession = null;
        int sessionIndex = -1;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getSessionId() == sessionId) {
                targetSession = sessions.get(i);
                sessionIndex = i;
                break;
            }
        }

        if (targetSession == null) {
            targetSession = sessionDAO.findById(sessionId);
            if (targetSession == null) {
                throw new VaxiCareException("Session #" + sessionId + " not found.");
            }
        }

        if (targetSession.isFull() || targetSession.getAvailableSlots() <= 0) {
            throw new SessionFullException(sessionId, targetSession.getCapacity());
        }

        // 3. Clinical Dose History & Polymorphic Vaccine Instantiation (Dynamic Method Dispatch)
        int currentDoseCount = doseDAO.getLatestDoseNumber(beneficiaryId, vaccineName);
        int nextDoseNo = currentDoseCount + 1;

        Vaccine vax;
        if ("ChildCare".equalsIgnoreCase(vaccineName) || ben.getAge() <= 17) {
            vax = new ChildVaccine(vaccineName, "BATCH-CH-" + System.currentTimeMillis() % 1000);
        } else if (nextDoseNo >= 3 || "BoosterCare".equalsIgnoreCase(vaccineName)) {
            boolean primaryCompleted = (currentDoseCount >= 2);
            vax = new BoosterVaccine(vaccineName, "BATCH-BO-" + System.currentTimeMillis() % 1000, primaryCompleted);
        } else {
            vax = new AdultVaccine(vaccineName, "BATCH-AD-" + System.currentTimeMillis() % 1000);
        }

        // Dynamic Method Dispatch: checks age and booster eligibility
        if (!vax.eligibility(ben.getAge())) {
            if (vax instanceof BoosterVaccine && currentDoseCount < 2) {
                throw new IneligibleAgeException(ben.getAge(),
                        "Booster vaccine requires completion of 2 primary doses. Current doses completed: " + currentDoseCount);
            }
            throw new IneligibleAgeException(ben.getAge(),
                    "Beneficiary age is ineligible for vaccine '" + vaccineName + "'.");
        }

        // 4. Dose Interval Enforcement
        if (currentDoseCount > 0) {
            DoseRecord latest = doseDAO.getLatestDose(beneficiaryId);
            if (latest != null) {
                LocalDate lastDoseDate = LocalDate.parse(latest.getDoseDate());
                LocalDate sessionDate = LocalDate.parse(targetSession.getSessionDate());
                long daysSince = ChronoUnit.DAYS.between(lastDoseDate, sessionDate);
                int requiredInterval = vax.intervalDays(nextDoseNo);

                if (daysSince < requiredInterval) {
                    int remaining = (int) (requiredInterval - daysSince);
                    String nextDue = vax.nextDueDate(latest.getDoseDate(), nextDoseNo);
                    throw new DoseNotDueException(
                            "Dose #" + nextDoseNo + " is not yet due. Required interval is " + requiredInterval + " days.",
                            remaining, nextDue);
                }
            }
        }

        // 5. Reserve slot and update records
        boolean updated = sessionDAO.incrementBooked(sessionId);
        if (!updated) {
            throw new SessionFullException(sessionId, targetSession.getCapacity());
        }

        targetSession.incrementBooked();
        if (sessionIndex >= 0 && sessionIndex < slotsRemainingPerSession.length) {
            slotsRemainingPerSession[sessionIndex] = targetSession.getAvailableSlots();
        }

        // Add to vaccination execution queue (LinkedList)
        synchronized (vaccinationQueue) {
            vaccinationQueue.add(ben);
            vaccinationQueue.notifyAll();
        }

        System.out.println(String.format("[VaxiCareService] Slot booked successfully! Ben: %s -> Session #%d (%s). Slots left: %d",
                ben.getName(), sessionId, targetSession.getSessionDate(), targetSession.getAvailableSlots()));

        return true;
    }

    /**
     * Administers a vaccine dose, generates non-overridable certificate number,
     * logs dose record to SQLite database, updates today's vaccinated set, and records vial consumption.
     */
    public synchronized DoseRecord recordVaccination(String beneficiaryId, String vaccineName, int sessionId)
            throws VaxiCareException, SQLException {

        Beneficiary ben = lookup.get(beneficiaryId);
        if (ben == null) {
            ben = beneficiaryDAO.findById(beneficiaryId);
            if (ben == null) {
                throw new VaxiCareException("Beneficiary #" + beneficiaryId + " not found.");
            }
            lookup.put(beneficiaryId, ben);
        }

        int currentDoseCount = doseDAO.getLatestDoseNumber(beneficiaryId, vaccineName);
        int nextDoseNo = currentDoseCount + 1;

        // Dynamic Method Dispatch
        Vaccine vax;
        if ("ChildCare".equalsIgnoreCase(vaccineName) || ben.getAge() <= 17) {
            vax = new ChildVaccine(vaccineName, "BATCH-CH-" + (System.currentTimeMillis() % 1000));
        } else if (nextDoseNo >= 3 || "BoosterCare".equalsIgnoreCase(vaccineName)) {
            vax = new BoosterVaccine(vaccineName, "BATCH-BO-" + (System.currentTimeMillis() % 1000), currentDoseCount >= 2);
        } else {
            vax = new AdultVaccine(vaccineName, "BATCH-AD-" + (System.currentTimeMillis() % 1000));
        }

        if (!vax.eligibility(ben.getAge())) {
            throw new IneligibleAgeException(ben.getAge(), "Ineligible for vaccine " + vaccineName);
        }

        // Draw dose from vial
        vialManager.drawDose(Thread.currentThread().getName());

        String today = LocalDate.now().toString();
        String nextDue = (nextDoseNo < vax.getDosesRequired()) ? vax.nextDueDate(today, nextDoseNo + 1) : "SERIES-COMPLETED";
        String certNo = vax.certificateNumber();

        DoseRecord record = new DoseRecord(0, beneficiaryId, vaccineName, nextDoseNo, today, nextDue, certNo);
        int insertedId = doseDAO.insert(record);
        record.setDoseId(insertedId);

        // Update tracking collections
        vaccinatedToday.add(beneficiaryId);
        if (!"SERIES-COMPLETED".equals(nextDue)) {
            dueDates.add(nextDue);
        }

        System.out.println(String.format("[VaxiCareService] Dose #%d administered to %s (%s). Certificate: %s",
                nextDoseNo, ben.getName(), beneficiaryId, certNo));

        return record;
    }

    /**
     * Purges already-vaccinated beneficiaries from reminder list using an explicit Iterator and iterator.remove().
     * Demonstrates Unit IV: Collection Iterator interface.
     */
    public List<Beneficiary> purgeAlreadyVaccinated(List<Beneficiary> candidateList) {
        if (candidateList == null) {
            return new ArrayList<>();
        }
        Iterator<Beneficiary> iterator = candidateList.iterator();
        while (iterator.hasNext()) {
            Beneficiary b = iterator.next();
            if (vaccinatedToday.contains(b.getBenId())) {
                System.out.println("[VaxiCareService] Purging already vaccinated beneficiary from reminder list: " + b.getBenId());
                iterator.remove(); // Explicit iterator.remove()
            }
        }
        return candidateList;
    }

    /**
     * Scans and retrieves all beneficiaries whose next dose is due tomorrow.
     */
    public List<Beneficiary> getDueTomorrowBeneficiaries() {
        List<Beneficiary> results = new ArrayList<>();
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            String tomorrowStr = tomorrow.toString();

            List<DoseRecord> allDoses = doseDAO.findAll();
            for (DoseRecord d : allDoses) {
                if (tomorrowStr.equals(d.getNextDue())) {
                    Beneficiary b = lookup.get(d.getBenId());
                    if (b == null) {
                        b = beneficiaryDAO.findById(d.getBenId());
                        if (b != null) lookup.put(b.getBenId(), b);
                    }
                    if (b != null && !results.contains(b)) {
                        results.add(b);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[VaxiCareService] Error fetching due tomorrow records: " + e.getMessage());
        }
        // Purge any already vaccinated today
        return purgeAlreadyVaccinated(results);
    }

    /**
     * Performs binary file backup of all doses to doses.dat using BinaryBackupManager.
     */
    public void backupDosesToBinary(String filePath) throws IOException, SQLException {
        List<DoseRecord> allDoses = doseDAO.findAll();
        backupManager.writeBackup(allDoses, filePath);
    }

    /**
     * Restores doses from binary file and verifies count match against SQLite.
     */
    public boolean restoreDosesAndVerify(String filePath) throws IOException, SQLException {
        int sqliteCount = doseDAO.count();
        List<DoseRecord> restored = backupManager.restoreFromBinary(filePath);
        return backupManager.verifyRestoredCount(sqliteCount, restored.size());
    }

    // Accessors for Collections and Components
    public List<Session> getSessions() {
        return sessions;
    }

    public List<Beneficiary> getVaccinationQueue() {
        return vaccinationQueue;
    }

    public List<String> getVialLogs() {
        return vialManager.getVialLogs();
    }

    public Set<String> getVaccinatedToday() {
        return vaccinatedToday;
    }

    public Set<String> getDueDates() {
        return dueDates;
    }

    public Map<String, Beneficiary> getLookup() {
        return lookup;
    }

    public Map<String, Integer> getIntervalTable() {
        return intervalTable;
    }

    public int[] getSlotsRemainingPerSession() {
        return slotsRemainingPerSession;
    }

    public BeneficiaryDAO getBeneficiaryDAO() {
        return beneficiaryDAO;
    }

    public SessionDAO getSessionDAO() {
        return sessionDAO;
    }

    public DoseDAO getDoseDAO() {
        return doseDAO;
    }

    public VialManager getVialManager() {
        return vialManager;
    }

    public BinaryBackupManager getBackupManager() {
        return backupManager;
    }

    public ReminderTask getReminderTask() {
        return reminderTask;
    }
}
