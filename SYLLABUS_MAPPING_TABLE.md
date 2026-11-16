# VaxiCare: Syllabus Mapping Table (Java Units I – IV)

This document maps every core topic from standard undergraduate Computer Science Java programming syllabi (Units I through IV) directly to its implementation in the **VaxiCare: Smart Vaccination Slot Booking and Dose Tracking System** codebase.

---

## Unit I: Object-Oriented Programming Fundamentals, Language Basics & Classes

| Syllabus Topic | Specific Concept | Package & Class | Exact Method / Field / Line | Description / Architectural Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **OOP Principles** | Abstraction | `com.vaxicare.model.Vaccine`<br>`com.vaxicare.model.Schedulable` | `public abstract int intervalDays(...)`<br>`nextDueDate(...)` | Exposes required vaccination contracts while hiding brand-specific scheduling formulas. |
| **OOP Principles** | Encapsulation | `com.vaxicare.model.Beneficiary`<br>`com.vaxicare.model.Session` | Private attributes with getters/setters and domain mutators | Encapsulates citizen demographic data and session capacity state. |
| **OOP Principles** | Inheritance | `com.vaxicare.model.ChildVaccine`<br>`com.vaxicare.model.AdultVaccine` | `extends Vaccine` | Establishes specialized clinical hierarchies from the abstract `Vaccine` base. |
| **OOP Principles** | Polymorphism | `com.vaxicare.service.VaxiCareService` | `Vaccine vax = ...`<br>`vax.eligibility(...)` | Dynamic runtime dispatch to evaluate eligibility and dose intervals per vaccine variant. |
| **Java Platform** | Compilation & CLASSPATH | Build scripts / Command line | `javac -d out -cp "lib/*"`<br>`java -cp "out;lib/*"` | Demonstrates compilation to bytecode (`.class`) and execution across user and library classpaths. |
| **Primitive Data Types** | Primitive Variables & Literals | `com.vaxicare.model.Session`<br>`com.vaxicare.threads.VialManager` | `int capacity`, `int booked`<br>`long vialOpenedTimeMillis` | Explicit primitive sizing (`int`, `long`, `boolean`) for high-throughput counters. |
| **Arrays** | 1D Primitive Array | `com.vaxicare.service.VaxiCareService` | `int[] slotsRemainingPerSession` | Primitive array tracking real-time available capacity indexed by session sequence. |
| **Arrays** | String Array Constant | `com.vaxicare.model.Beneficiary` | `public static final String[] AGE_GROUPS` | Array of constant demographic classification strings. |
| **Control Flow** | Conditional Branching (`if-else`) | `com.vaxicare.model.Beneficiary` | `determineAgeGroup(int age)` | Evaluates age boundaries (`<=17`, `<=59`, `60+`) to categorize citizens. |
| **Control Flow** | Loops (`while`, `for`, `for-each`) | `com.vaxicare.dao.DataSeeder`<br>`com.vaxicare.threads.VaccinatorThread` | `for (int i = 0; i < 20; i++)`<br>`while (queue.isEmpty() && running)` | Controlled iteration for seed generation and thread queue synchronization. |
| **Methods** | Value Return & Parameter Passing | `com.vaxicare.model.Schedulable` | `String nextDueDate(String lastDose, int doseNo)` | Pass-by-value semantics for date calculations and due-date projection. |
| **Method Overloading** | Overloaded Business Logic | `com.vaxicare.service.VaxiCareService` | 1. `bookSlot(String benId)`<br>2. `bookSlot(String benId, int sessId)`<br>3. `bookSlot(String benId, int sessId, String vax)` | 3 overloaded slot booking methods providing auto-match, session-specific, and fully-specified flows. |
| **Constructors** | Constructor Overloading | `com.vaxicare.model.Beneficiary` | 1. `Beneficiary(String name)`<br>2. `Beneficiary(String name, int age)`<br>3. `Beneficiary(name, age, id, phone)` | Flexible instantiation for partial registrations and full citizen records. |
| **Constructors** | Constructor Chaining (`this`) | `com.vaxicare.model.Beneficiary` | `this(name, 18);`<br>`this(name, age, id, phone);` | Eliminates duplicated validation code by routing upstream constructors to master constructor. |
| **Access Specifiers** | `private` Modifier | `com.vaxicare.model.Vaccine` | `private String lastCalculatedDueDate;` | Strictly protects internal calculation state from external modification. |
| **Access Specifiers** | Package-Private (Default) | `com.vaxicare.model.Vaccine` | `String batchCode;` | Accessible to fellow package models (`ChildVaccine`, `AdultVaccine`) but shielded from outside packages. |
| **Access Specifiers** | `protected` Modifier | `com.vaxicare.model.AdultVaccine`<br>`com.vaxicare.model.Vaccine` | `protected int minimumAge = 18;`<br>`protected String name;` | Allows direct visibility in subclasses while encapsulating from unassociated packages. |
| **Access Specifiers** | `public` Modifier | All Model & Service Classes | `public class Beneficiary`, `public boolean bookSlot(...)` | Open interface boundary for application-wide service calls. |
| **Static Keyword** | Static Fields & Counters | `com.vaxicare.model.Beneficiary`<br>`com.vaxicare.model.Vaccine` | `private static int idCounter = 5500;`<br>`private static int certCounter = 2000;` | Central sequential identifier generators shared across all class instances. |
| **Static Keyword** | Static Utility Methods | `com.vaxicare.model.Beneficiary` | `public static boolean isValidPhone(String phone)`<br>`public static String determineAgeGroup(int age)` | Stateless validation functions callable without object instantiation. |
| **Static Keyword** | Static Initializer Block | `com.vaxicare.dao.DatabaseManager` | `static { Class.forName("org.sqlite.JDBC"); }` | One-time driver class loading when the class loader initializes `DatabaseManager`. |

---

## Unit II: Inheritance, Polymorphism, Abstract Classes, Interfaces & Packages

| Syllabus Topic | Specific Concept | Package & Class | Exact Method / Field / Line | Description / Architectural Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Inheritance** | Single Inheritance | `com.vaxicare.model.AdultVaccine` | `public class AdultVaccine extends Vaccine` | Inherits scheduling attributes and methods from base `Vaccine`. |
| **Inheritance** | Multilevel Inheritance | `com.vaxicare.model.BoosterVaccine` | `public final class BoosterVaccine extends AdultVaccine` | Extends `AdultVaccine`, which in turn extends abstract `Vaccine`. |
| **Inheritance** | Hierarchical Inheritance | `com.vaxicare.model.ChildVaccine`<br>`com.vaxicare.model.AdultVaccine` | Both extend `com.vaxicare.model.Vaccine` | Multiple distinct clinical variants deriving from common vaccine ancestor. |
| **Super Keyword** | Superclass Constructor Call | `com.vaxicare.model.AdultVaccine` | `super(name, dosesRequired, dosesPerVial, batchCode);` | Explicit initialization of inherited fields in superclass. |
| **Super Keyword** | Superclass Method Invocation | `com.vaxicare.model.BoosterVaccine` | `super.eligibility(age) && primarySeriesCompleted` | Reuses adult age eligibility checks while compounding booster completion rules. |
| **Method Overriding** | Polymorphic Overriding | `com.vaxicare.model.ChildVaccine`<br>`com.vaxicare.model.AdultVaccine` | `@Override public int intervalDays(int doseNo)`<br>`@Override public boolean eligibility(int age)` | Replaces general intervals with pediatric (28d/42d) and adult rules (28d, age>=18). |
| **Dynamic Dispatch** | Runtime Polymorphism | `com.vaxicare.service.VaxiCareService` | `Vaccine vax = new ChildVaccine(...);`<br>`vax.intervalDays(nextDoseNo);` | JVM executes the concrete overridden method based on actual runtime instance type. |
| **Abstract Classes** | Abstract Class Definition | `com.vaxicare.model.Vaccine` | `public abstract class Vaccine implements Schedulable` | Enforces structural base for all clinical vaccines without allowing direct instantiation. |
| **Abstract Methods** | Mandatory Contract | `com.vaxicare.model.Vaccine` | `public abstract int intervalDays(int doseNumber);` | Forces each subclass to implement its specific regulatory dose spacing. |
| **Final Keyword** | Final Class | `com.vaxicare.model.BoosterVaccine` | `public final class BoosterVaccine extends AdultVaccine` | Prevents further inheritance or modification of booster logic. |
| **Final Keyword** | Final Method | `com.vaxicare.model.Vaccine` | `public final String certificateNumber()` | Secures tamper-proof vaccination certificate numbering (`VC-26-XXXX`). |
| **Final Keyword** | Final Constants | `com.vaxicare.model.Vaccine` | `public static final int VIAL_OPEN_HOURS = 6;` | Immutable regulatory open-vial discard limit. |
| **Object Methods** | Object Overrides | `com.vaxicare.model.Beneficiary`<br>`com.vaxicare.model.Session`<br>`com.vaxicare.model.DoseRecord` | `@Override public String toString()`<br>`@Override public boolean equals(Object o)`<br>`@Override public int hashCode()` | Proper entity equality comparison, collection hashing, and string rendering. |
| **Interfaces** | Interface Definition | `com.vaxicare.model.Schedulable` | `public interface Schedulable { ... }` | Pure contract for slot verification, eligibility, and projected due dates. |
| **Interfaces** | Interface Implementation | `com.vaxicare.model.Vaccine` | `public abstract class Vaccine implements Schedulable` | Provides shared implementation of interface methods (`nextDueDate`, `isDue`, `bookSlot`). |
| **Packages** | Modular Architecture | `com.vaxicare.*` (8 packages) | `package com.vaxicare.model;`, etc. | Strict isolation of exceptions, domain models, DAOs, threads, IO, services, UI, and main launcher. |

---

## Unit III: Exception Handling & Multithreading

| Syllabus Topic | Specific Concept | Package & Class | Exact Method / Field / Line | Description / Architectural Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Exception Hierarchy** | Custom Base Exception | `com.vaxicare.exception.VaxiCareException` | `public class VaxiCareException extends Exception` | Root checked exception for domain-level failure signaling. |
| **Exception Hierarchy** | Custom Business Exceptions | `com.vaxicare.exception.DoseNotDueException`<br>`com.vaxicare.exception.SessionFullException`<br>`com.vaxicare.exception.IneligibleAgeException` | Derived from `VaxiCareException` with contextual fields (`daysRemaining`, `capacity`, `age`) | Expressive error reporting for interval violations, center saturation, and age ineligibility. |
| **Uncaught Exceptions** | Default Exception Handler | `com.vaxicare.exception.UncaughtDemo` | `int result = totalVials / activeDistributionCenters;` (Line 19) | Demonstrates runtime `ArithmeticException: / by zero` when unhandled by try-catch. |
| **Keywords** | `throw` and `throws` | `com.vaxicare.service.VaxiCareService` | `throws VaxiCareException, SQLException`<br>`throw new SessionFullException(...)` | Explicitly declares and throws checked domain exceptions up the execution stack. |
| **Keywords** | Multiple `catch` Blocks | `com.vaxicare.ui.VaxiCareFrame` | `catch (DoseNotDueException dnde) ...`<br>`catch (SessionFullException sfe) ...`<br>`catch (SQLException sqle) ...` | Granular error interception displaying specialized AWT modal dialogs. |
| **Keywords** | `finally` Resource Cleanup | `com.vaxicare.dao.DatabaseManager`<br>`com.vaxicare.dao.BeneficiaryDAO` | `finally { closeStatement(ps); closeConnection(conn); }` | Guarantees release of JDBC connections and statements regardless of query outcome. |
| **Keywords** | Nested `try-catch-finally` | `com.vaxicare.io.BinaryBackupManager` | `restoreFromBinary(String filePath)` | Outer block manages `FileInputStream` lifecycle; inner block manages byte deserialization. |
| **Multithreading** | Extending `Thread` Class | `com.vaxicare.threads.VaccinatorThread` | `public class VaccinatorThread extends Thread` | Models autonomous clinical vaccinator drawing doses and administering injections. |
| **Multithreading** | Implementing `Runnable` | `com.vaxicare.threads.ReminderTask` | `public class ReminderTask implements Runnable` | Standalone task scanning due dates and inspecting thread ecosystem states. |
| **Thread Lifecycle** | Thread States Inspection | `com.vaxicare.threads.VaccinatorThread`<br>`com.vaxicare.threads.ReminderTask` | `getState()` (`RUNNABLE`, `WAITING`, `TERMINATED`) | Real-time monitoring of thread lifecycles during active vaccination and idle waiting. |
| **Thread Priorities** | Senior vs General Priority | `com.vaxicare.service.VaxiCareService` | `Thread.MAX_PRIORITY` (10) for Senior Vaccinator<br>`Thread.NORM_PRIORITY` (5) for General Vaccinator | Gives higher CPU scheduling preference to senior citizen patient processing. |
| **Synchronization** | Synchronized Methods | `com.vaxicare.threads.VialManager` | `public synchronized boolean drawDoseSynchronized(String name)` | Mutual exclusion preventing simultaneous threads from corrupting vial doses remaining. |
| **Synchronization** | Synchronized Blocks | `com.vaxicare.threads.VaccinatorThread` | `synchronized (queue) { while (queue.isEmpty()) queue.wait(); }` | Thread-safe queue consumption avoiding lost updates and race conditions. |
| **Concurrency Bugs** | Race Condition Toggle | `com.vaxicare.threads.VialManager` | `drawDoseUnsynchronized(String threadName)` | Deliberately skips locks with sleep/yield to demonstrate lost updates under concurrency. |
| **Inter-Thread Comm** | `wait()`, `notifyAll()` | `com.vaxicare.threads.VaccinatorThread`<br>`com.vaxicare.threads.ReminderTask` | `queue.wait();`<br>`reminderLock.notifyAll();` | Producer-consumer coordination between waiting vaccinators and slot scheduling queues. |
| **Daemon Threads** | Background Daemon Service | `com.vaxicare.service.VaxiCareService` | `reminderThread.setDaemon(true);` | Runs reminder scanner as a background service that automatically terminates upon GUI exit. |

---

## Unit IV: Java I/O Streams, Collections Framework, Pure AWT Event Handling & JDBC

| Syllabus Topic | Specific Concept | Package & Class | Exact Method / Field / Line | Description / Architectural Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Byte Streams** | File Output Byte Stream | `com.vaxicare.io.BinaryBackupManager` | `FileOutputStream fos = new FileOutputStream(filePath);` | Writes raw binary byte streams without character transcoding (`Reader`/`Writer` strictly excluded). |
| **Byte Streams** | File Input Byte Stream | `com.vaxicare.io.BinaryBackupManager` | `FileInputStream fis = new FileInputStream(file);` | Reads binary byte sequences directly from disk into byte buffers. |
| **NIO Buffer** | Direct ByteBuffer Handling | `com.vaxicare.io.BinaryBackupManager` | `ByteBuffer.allocate(4).putInt(val).array();`<br>`ByteBuffer.wrap(buf).getInt();` | Manual serialization and deserialization of integer primitives and length prefixes. |
| **String Encoding** | Manual UTF-8 Byte Conversion | `com.vaxicare.io.BinaryBackupManager` | `str.getBytes(StandardCharsets.UTF_8)`<br>`new String(utfBytes, StandardCharsets.UTF_8)` | Low-level length-prefixed binary string serialization without `ObjectOutputStream`. |
| **Collection 1** | `List` -> `ArrayList` | `com.vaxicare.service.VaxiCareService` | `List<Session> sessions = new ArrayList<>()` | Fast index-based access and iteration over active vaccination sessions. |
| **Collection 2** | `List` -> `LinkedList` | `com.vaxicare.service.VaxiCareService` | `List<Beneficiary> vaccinationQueue = new LinkedList<>()` | Efficient FIFO queue insertion and polling for patient administration pipeline. |
| **Collection 3** | `List` -> `Vector` | `com.vaxicare.service.VaxiCareService`<br>`com.vaxicare.threads.VialManager` | `List<String> vialLogs = new Vector<>()` | Legacy thread-safe synchronized list for append-only vial audit logging. |
| **Collection 4** | `Set` -> `HashSet` | `com.vaxicare.service.VaxiCareService` | `Set<String> vaccinatedToday = new HashSet<>()` | O(1) membership lookup ensuring duplicate daily doses are prevented. |
| **Collection 5** | `Set` -> `TreeSet` | `com.vaxicare.service.VaxiCareService` | `Set<String> dueDates = new TreeSet<>()` | Sorted, unique set of chronological projected vaccination due dates. |
| **Collection 6** | `Map` -> `HashMap` | `com.vaxicare.service.VaxiCareService` | `Map<String, Beneficiary> lookup = new HashMap<>()` | High-performance key-value mapping from citizen ID (`"B5531"`) to `Beneficiary` entity. |
| **Collection 7** | `Map` -> `Hashtable` | `com.vaxicare.service.VaxiCareService` | `Map<String, Integer> intervalTable = new Hashtable<>()` | Synchronized, thread-safe key-value store for regulatory vaccine dose intervals. |
| **Iterator** | Explicit `Iterator.remove()` | `com.vaxicare.service.VaxiCareService` | `Iterator<Beneficiary> it = list.iterator();`<br>`while(it.hasNext()) { ... it.remove(); }` | Purges already-vaccinated beneficiaries from reminder lists safely without `ConcurrentModificationException`. |
| **AWT Components** | Pure AWT Interface (No Swing) | `com.vaxicare.ui.VaxiCareFrame` | `Frame`, `Panel`, `Label`, `Button`, `TextField`, `TextArea`, `Checkbox`, `Choice`, `List`, `Dialog` | Pure heavyweight native GUI components implementing clinical interface. |
| **Layout Managers** | Multi-Layout Composition | `com.vaxicare.ui.VaxiCareFrame` | `CardLayout`, `BorderLayout`, `GridLayout`, `FlowLayout` | Responsive window organization switching among Registration, Slots, Vaccination, and Reports views. |
| **Delegation Events** | `ActionListener` | `com.vaxicare.ui.VaxiCareFrame` | `implements ActionListener`<br>`actionPerformed(ActionEvent e)` | Dispatches button click events for registration, slot booking, dose recording, and reports. |
| **Delegation Events** | `ItemListener` | `com.vaxicare.ui.VaxiCareFrame` | `implements ItemListener`<br>`itemStateChanged(ItemEvent e)` | Responds to concurrency mode checkbox toggles and dropdown choice selections. |
| **Delegation Events** | `KeyListener` & Shortcuts | `com.vaxicare.ui.VaxiCareFrame` | `implements KeyListener`<br>`keyPressed(KeyEvent e)` | Keyboard shortcuts: `ENTER` (search beneficiary), `Ctrl+V` (administer dose), `F4` (daily report). |
| **Event Adapters** | `WindowAdapter` | `com.vaxicare.ui.VaxiCareFrame` | `addWindowListener(new WindowAdapter() { ... })` | Intercepts window close events to cleanly shut down background threads and close DB. |
| **Event Adapters** | `MouseAdapter` | `com.vaxicare.ui.VaxiCareFrame` | `benAwtList.addMouseListener(new MouseAdapter() { ... })` | Click listener on `java.awt.List` rows to automatically populate form fields. |
| **JDBC Architecture** | Driver & Connection | `com.vaxicare.dao.DatabaseManager` | `DriverManager.getConnection("jdbc:sqlite:vaccination.db")` | Connects directly to embedded SQLite database engine. |
| **JDBC Architecture** | DDL Table Initialization | `com.vaxicare.dao.DatabaseManager` | `stmt.executeUpdate(createBeneficiaries);` | Auto-creates schema tables (`beneficiaries`, `sessions`, `doses`) on first run. |
| **JDBC PreparedStatement**| 100% Parameterized CRUD | `com.vaxicare.dao.BeneficiaryDAO`<br>`com.vaxicare.dao.SessionDAO`<br>`com.vaxicare.dao.DoseDAO` | `conn.prepareStatement("INSERT INTO beneficiaries ...")`<br>`ps.setString(1, ...); ps.executeUpdate();` | Complete Create, Retrieve, Update, and Delete operations shielded from SQL Injection. |
| **JDBC Keys** | Generated Primary Keys | `com.vaxicare.dao.SessionDAO`<br>`com.vaxicare.dao.DoseDAO` | `conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)` | Captures autoincrement primary keys (`session_id`, `dose_id`) directly into model instances. |

---

## Verification & Execution Summary

- **JDK Version**: OpenJDK 21 LTS (`21.0.12`)
- **Database**: SQLite 3 (`vaccination.db`) via `org.xerial:sqlite-jdbc:3.45.2.0`
- **UI Framework**: Pure `java.awt.*` (0% Swing, 0% JavaFX)
- **Backup Parity**: 100% round-trip match between SQLite records and binary `doses.dat` byte stream.
