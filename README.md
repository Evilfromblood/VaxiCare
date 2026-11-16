# VaxiCare: Smart Vaccination Slot Booking & Dose Tracking System

![Java](https://img.shields.io/badge/Java-JDK%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![UI](https://img.shields.io/badge/UI-Pure%20AWT-blue?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-SQLite%203-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Modular%208--Package-success?style=for-the-badge)

**VaxiCare** is a clinical vaccination management and slot booking system built entirely in **pure Core Java (JDK 21)** using standard Java SE libraries (`java.awt`, `java.io`, `java.sql`, `java.util`, `java.time`) and `org.xerial:sqlite-jdbc`.

It strictly adheres to core Java principles without external UI frameworks (zero Swing, zero JavaFX) or build-tool abstractions (zero Maven/Gradle dependencies), serving as an exhaustive reference implementation covering Units I–IV of standard undergraduate Computer Science Java curricula.

---

## 🌟 Key Features

1. **Modular 8-Package Architecture**: Deliberate encapsulation and access control across `exception`, `model`, `dao`, `threads`, `io`, `service`, `ui`, and `main` packages.
2. **Clinical Vaccine Hierarchy & Dynamic Method Dispatch**:
   - `Vaccine` (abstract base implementing `Schedulable`)
   - `ChildVaccine` (pediatric rules: Dose 2 at 28d, Dose 3 at 42d, age $\le 17$)
   - `AdultVaccine` (minimum age 18, 28d interval)
   - `BoosterVaccine` (`final` class enforcing 2-dose primary series completion, 180d interval)
   - Tamper-proof non-overridable certificate numbers (`VC-26-XXXX`).
3. **Robust Domain Exceptions**:
   - `DoseNotDueException` (with days remaining & projected due date)
   - `SessionFullException` (capacity validation)
   - `IneligibleAgeException` (demographic and booster criteria)
   - `UncaughtDemo` (standalone runtime `ArithmeticException` demo)
4. **Isolated JDBC Persistence (SQLite 3)**:
   - 100% `PreparedStatement` queries shielding against SQL injection.
   - Guaranteed resource closure in `finally` blocks.
   - Automated `DataSeeder` generating $\ge 22$ citizens (including `Sarojini`, `B5531`, age 64), 20 sessions, and 22 dose records (half due tomorrow).
5. **Multithreaded Clinical Simulation & Concurrency Controls**:
   - `VaccinatorThread` with priority scheduling (`Thread.MAX_PRIORITY` for senior citizens, `Thread.NORM_PRIORITY` for general).
   - Producer-consumer coordination via `synchronized(queue)`, `wait()`, and `notifyAll()`.
   - `VialManager` tracking opened vials, remaining doses, wastage, and 6-hour open-vial expiration.
   - Concurrency toggle contrasting thread-safe synchronized locking vs unsynchronized race conditions.
   - Background daemon `ReminderTask` scanning beneficiaries due tomorrow and monitoring thread states.
6. **Low-Level Binary Byte Backup (`doses.dat`)**:
   - Direct byte stream I/O using ONLY `FileOutputStream` and `FileInputStream`.
   - Manual `ByteBuffer` serialization of integers and length-prefixed UTF-8 payloads (strictly zero character streams or `ObjectOutputStream`).
   - Nested `try-catch-finally` blocks and 100% record count verification against SQLite.
7. **Pure AWT Graphical User Interface**:
   - Heavyweight interface using `CardLayout` across 4 panels: **Registration**, **Slots**, **Vaccination**, and **Reports**.
   - Event handling via `ActionListener`, `ItemListener`, `KeyListener`, `WindowAdapter`, and `MouseAdapter`.
   - Keyboard Shortcuts:
     - `ENTER`: Fetch beneficiary by ID.
     - `Ctrl+V`: Administer dose.
     - `F4`: Generate Daily Coverage Report.

---

## 📁 Project Structure

```
VaxiCare/
├── lib/
│   ├── sqlite-jdbc-3.45.2.0.jar
│   ├── slf4j-api-2.0.12.jar
│   └── slf4j-nop-2.0.12.jar
├── src/
│   └── com/
│       └── vaxicare/
│           ├── exception/
│           │   ├── VaxiCareException.java
│           │   ├── DoseNotDueException.java
│           │   ├── SessionFullException.java
│           │   ├── IneligibleAgeException.java
│           │   └── UncaughtDemo.java
│           ├── model/
│           │   ├── Schedulable.java
│           │   ├── Vaccine.java
│           │   ├── ChildVaccine.java
│           │   ├── AdultVaccine.java
│           │   ├── BoosterVaccine.java
│           │   ├── Beneficiary.java
│           │   ├── Session.java
│           │   └── DoseRecord.java
│           ├── dao/
│           │   ├── DatabaseManager.java
│           │   ├── BeneficiaryDAO.java
│           │   ├── SessionDAO.java
│           │   ├── DoseDAO.java
│           │   └── DataSeeder.java
│           ├── threads/
│           │   ├── VialManager.java
│           │   ├── VaccinatorThread.java
│           │   └── ReminderTask.java
│           ├── io/
│           │   └── BinaryBackupManager.java
│           ├── service/
│           │   └── VaxiCareService.java
│           ├── ui/
│           │   └── VaxiCareFrame.java
│           └── main/
│               └── VaxiCareApp.java
├── doses.dat
├── vaccination.db
├── SYLLABUS_MAPPING_TABLE.md
└── README.md
```

---

## 🚀 Quick Start & Execution

### Prerequisites
- Java Development Kit (JDK 21 or higher)
- Operating System: Windows, Linux, or macOS

### 1. Compile the Project
From the repository root directory:

**Windows (PowerShell):**
```powershell
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out }
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { ($_.FullName.Substring($PWD.Path.Length + 1) -replace '\\', '/') } | Set-Content -Encoding ascii sources.txt
javac -d out -cp "lib/*" "@sources.txt"
```

**Linux / macOS (Bash):**
```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out -cp "lib/*" @sources.txt
```

### 2. Run Comprehensive Automated Verification (Headless)
Executes database checks, dynamic dispatch validation, custom exception triggers, binary backup parity checks, and multithreading verification:
```bash
# Windows
java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp --verify

# Linux / macOS
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp --verify
```

### 3. Launch the Pure AWT Graphical Interface
```bash
# Windows
java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp

# Linux / macOS
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp
```

### 4. Run Uncaught Arithmetic Exception Demonstration
```bash
# Windows
java -cp "out;lib/*" com.vaxicare.exception.UncaughtDemo

# Linux / macOS
java -cp "out:lib/*" com.vaxicare.exception.UncaughtDemo
```

---

## ⌨️ Graphical Interface Navigation & Shortcuts

| Action | Shortcut / Trigger | Description |
| :--- | :--- | :--- |
| **Search Beneficiary** | `ENTER` (in ID field) | Fetches citizen record by ID and populates all form fields. |
| **Administer Dose** | `Ctrl + V` | Validates intervals, draws vial dose, generates certificate, updates DB. |
| **Coverage Report** | `F4` | Compiles and switches to the live Daily Coverage Report. |
| **Select Row** | Left Click on List | MouseAdapter transfers clicked row data to editing inputs. |
| **Toggle Sync** | Checkbox click | Switches between synchronized thread safety and unsynchronized race mode. |

---

## 📚 Academic Syllabus Mapping

For full academic documentation mapping Java Units I–IV topics (constructors, dynamic dispatch, collections, JDBC, AWT event delegation, byte streams, multithreading) to exact code files and methods, refer to:
👉 **[SYLLABUS_MAPPING_TABLE.md](SYLLABUS_MAPPING_TABLE.md)**

---

## 📄 License
This project is released under the MIT License.
