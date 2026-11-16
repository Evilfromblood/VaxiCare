# VaxiCare: Smart Vaccination Slot Booking & Dose Tracking System

![Java](https://img.shields.io/badge/Java-JDK%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![UI](https://img.shields.io/badge/UI-Pure%20AWT-blue?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-SQLite%203-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Modular%208--Package-success?style=for-the-badge)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey?style=for-the-badge)

**VaxiCare** is a clinical vaccination management and slot booking system built entirely in **pure Core Java (JDK 21)** using standard Java SE libraries (`java.awt`, `java.io`, `java.sql`, `java.util`, `java.time`) and `org.xerial:sqlite-jdbc`.

It strictly adheres to core Java principles without external UI frameworks (zero Swing, zero JavaFX) or build-tool abstractions (zero Maven/Gradle dependencies), serving as an exhaustive reference implementation covering Units I–IV of standard undergraduate Computer Science Java curricula.

---

## ⚡ Quick Start: How to Make It Work (In 60 Seconds)

### 1. Prerequisites
- **Java Development Kit**: JDK 21 (or newer). Verify with:
  ```bash
  java -version
  javac -version
  ```
- **Git** (to clone the repository).

### 2. Clone the Repository
```bash
git clone https://github.com/Evilfromblood/VaxiCare.git
cd VaxiCare
```

### 3. One-Command Launchers

#### On Windows:
- **Run the GUI**: Double-click `run.bat` or run:
  ```powershell
  .\run.bat
  ```
- **Run Automated Headless Tests**: Double-click `verify.bat` or run:
  ```powershell
  .\verify.bat
  ```

#### On Linux / macOS:
```bash
# Make scripts executable
chmod +x run.sh verify.sh

# Run the GUI
./run.sh

# Run Automated Headless Tests
./verify.sh
```

---

## 🛠️ Manual Step-by-Step Compilation & Execution

If you prefer executing commands directly in your terminal without runner scripts:

### Step 1: Compile all Packages into `out/`

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

---

### Step 2: Run the Application

#### A. Launch the Graphical User Interface (AWT Desktop App)
```bash
# Windows
java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp

# Linux / macOS
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp
```

#### B. Run the Comprehensive Headless Test Suite (`--verify`)
This mode tests database auto-seeding, all 3 overloaded booking methods, dynamic method dispatch, custom exception triggers, binary backup parity, and thread state logging without needing a display:
```bash
# Windows
java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp --verify

# Linux / macOS
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp --verify
```

#### C. Run the Uncaught ArithmeticException Demonstration
Demonstrates Unit III runtime exception handling and default JVM handler (`45 / 0` vials):
```bash
# Windows
java -cp "out;lib/*" com.vaxicare.exception.UncaughtDemo

# Linux / macOS
java -cp "out:lib/*" com.vaxicare.exception.UncaughtDemo
```

---

## 🖥️ How to Use the Graphical User Interface

The AWT desktop application consists of 4 interactive cards switched via the top navigation bar:

```
+-------------------------------------------------------------------------+
|                  VaxiCare 2.0: Smart Clinical Vaccination               |
+-------------------------------------------------------------------------+
| [1. Registration]  [2. Slot Booking]  [3. Vaccination]  [4. Reports]   |
+-------------------------------------------------------------------------+
|                                                                         |
|                          ACTIVE CARD PANEL AREA                         |
|                                                                         |
+-------------------------------------------------------------------------+
| Status: [ENTER] Fetch Beneficiary | [Ctrl+V] Administer Dose | [F4] Report|
+-------------------------------------------------------------------------+
```

### 1. Panel 1: Citizen Registration (`CardLayout`: `REGISTRATION`)
* **Register New Citizen**: Enter Name, Age, Document ID, and 10-digit Phone. Click **"Register New Beneficiary"**.
  * Age classification (`Child (0-17)`, `Adult (18-59)`, `Senior Citizen (60+)`) is auto-computed.
* **Search Citizen**: Type a Beneficiary ID (e.g., `B5531`) and press **`ENTER`** or click **"Search by Ben ID"**.
* **Quick Load**: Single-click any row in the registered citizens list (`MouseAdapter`) to automatically populate the input fields.

### 2. Panel 2: Slot Booking (`CardLayout`: `SLOTS`)
* **Manual Booking**: Enter Beneficiary ID, select a Session and Vaccine Brand from dropdowns, and click **"Book Selected Slot"**.
* **Auto-Match Booking**: Enter Beneficiary ID and click **"Auto-Match First Available"** to invoke the overloaded single-argument `bookSlot(benId)` method.
* **Clinical Eligibility Checks**:
  * Prevents adults from booking pediatric `ChildCare`.
  * Prevents booster booking without completing 2 primary doses (`IneligibleAgeException`).
  * Enforces mandatory vaccine interval days (`DoseNotDueException`).
  * Rejects booking if session capacity is reached (`SessionFullException`).

### 3. Panel 3: Vaccination Administration & Vial Lab (`CardLayout`: `VACCINATION`)
* **Administer Dose**: Click **"Administer Dose"** or press **`Ctrl + V`**.
  * Draws 1 dose from the active physical vial.
  * Generates an official, non-overridable certificate number (`VC-26-XXXX`).
  * Records the dose in SQLite and updates the citizen's schedule.
* **Vial Expiration & Wastage**: Vials automatically discard remaining doses and record wastage if open for more than 6 hours (`Vaccine.VIAL_OPEN_HOURS`).
* **Concurrency Toggle**: Check/uncheck **"Enable Thread Locking"** to toggle between thread-safe `drawDoseSynchronized` and `drawDoseUnsynchronized` to witness simulated race conditions.
* **Binary Backup & Verification**:
  * Click **"Backup Doses (doses.dat)"** to write all records using raw `FileOutputStream` and `ByteBuffer`.
  * Click **"Restore & Verify doses.dat"** to read back using `FileInputStream` through nested `try-catch-finally` and verify 100% record count parity against SQLite.

### 4. Panel 4: Reports, Audit Logs & Thread Monitor (`CardLayout`: `REPORTS`)
* **Daily Coverage Report**: Press **`F4`** or click **"Daily Coverage Report"** to inspect total vaccinated citizens, vial efficiency, and remaining slot arrays (`int[] slotsRemainingPerSession`).
* **Due Tomorrow Reminders**: Displays all citizens scheduled for their next dose tomorrow.
* **Thread State Monitor**: Displays real-time inspection of background worker threads (`RUNNABLE`, `WAITING`, `TERMINATED`).
* **Vial Audit Logs**: Displays the complete history of vial openings, doses drawn, and wastage from the `Vector<String>` log collection.

---

## ⌨️ Keyboard Shortcuts Reference

| Shortcut | Context | Function |
| :--- | :--- | :--- |
| **`ENTER`** | Any ID Input Field | Fetches citizen by Beneficiary ID and populates all form fields. |
| **`Ctrl + V`** | Global Application | Administers dose to the active candidate and generates certificate. |
| **`F4`** | Global Application | Compiles and switches directly to the Daily Coverage Report. |

---

## 📁 Package & File Breakdown

```
VaxiCare/
├── lib/
│   ├── sqlite-jdbc-3.45.2.0.jar         # SQLite JDBC native driver
│   ├── slf4j-api-2.0.12.jar             # SLF4J logging API
│   └── slf4j-nop-2.0.12.jar             # NOP logger binding
├── src/
│   └── com/
│       └── vaxicare/
│           ├── exception/               # Custom and uncaught exception hierarchy
│           │   ├── VaxiCareException.java       (Base checked exception)
│           │   ├── DoseNotDueException.java     (Interval validation)
│           │   ├── SessionFullException.java    (Capacity validation)
│           │   ├── IneligibleAgeException.java  (Age & booster criteria)
│           │   └── UncaughtDemo.java            (Runtime 45/0 demo)
│           ├── model/                   # OOP domain entities and inheritance
│           │   ├── Schedulable.java             (Interface contract)
│           │   ├── Vaccine.java                 (Abstract base with final certificateNumber)
│           │   ├── ChildVaccine.java            (Pediatric intervals & eligibility)
│           │   ├── AdultVaccine.java            (Adult intervals & minimumAge = 18)
│           │   ├── BoosterVaccine.java          (Final class, primary series check, 180d)
│           │   ├── Beneficiary.java             (Overloaded constructors chained via this)
│           │   ├── Session.java                 (Session capacity model)
│           │   └── DoseRecord.java              (Administered dose record)
│           ├── dao/                     # 100% Parameterized JDBC persistence
│           │   ├── DatabaseManager.java         (Connection & schema lifecycle)
│           │   ├── BeneficiaryDAO.java          (Beneficiaries CRUD)
│           │   ├── SessionDAO.java              (Sessions CRUD)
│           │   ├── DoseDAO.java                 (Doses CRUD)
│           │   └── DataSeeder.java              (Auto-seeds >= 22 bens, 20 sess, 22 doses)
│           ├── threads/                 # Multithreading, priorities & concurrency
│           │   ├── VialManager.java             (Synchronized & unsynchronized vial draw)
│           │   ├── VaccinatorThread.java        (MAX_PRIORITY/NORM_PRIORITY queue workers)
│           │   └── ReminderTask.java            (Runnable background scanner with wait/notify)
│           ├── io/                      # Raw binary byte serialization
│           │   └── BinaryBackupManager.java     (FileOutputStream/FileInputStream via ByteBuffer)
│           ├── service/                 # Business orchestration & Collections
│           │   └── VaxiCareService.java         (7 Collections, 3 overloaded bookSlots, Iterator.remove)
│           ├── ui/                      # Pure AWT Graphical Interface
│           │   └── VaxiCareFrame.java           (CardLayout, Adapters, KeyListener, Dialogs)
│           └── main/                    # Application bootstrapper
│               └── VaxiCareApp.java             (Main entry & headless verification suite)
├── run.bat                              # One-click Windows runner
├── verify.bat                           # One-click Windows test runner
├── run.sh                               # One-click Linux/macOS runner
├── verify.sh                            # One-click Linux/macOS test runner
├── doses.dat                            # Binary byte backup file
├── vaccination.db                       # Pre-seeded SQLite database
├── SYLLABUS_MAPPING_TABLE.md             # Complete Java Units I–IV academic mapping
└── README.md                            # Comprehensive user manual
```

---

## ❓ Frequently Asked Questions (FAQ) & Troubleshooting

### 1. Can I run VaxiCare in a headless server or container?
**Yes.** If your environment has no graphical display (e.g., GitHub Actions, Linux SSH session, Docker), run:
```bash
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp --verify
```
The `--verify` flag runs the full suite in headless mode without initializing any AWT display components.

### 2. What happens on the first run if `vaccination.db` does not exist?
`DatabaseManager` automatically creates the tables (`beneficiaries`, `sessions`, `doses`), and `DataSeeder` automatically populates:
- **25 Beneficiaries** (including `Sarojini`, `B5531`, age 64).
- **20 Sessions** across multiple dates and vaccine brands.
- **22 Dose Records** (with exactly 11 scheduled for tomorrow).

### 3. How does the binary backup (`doses.dat`) differ from standard serialization?
It **does not** use `ObjectOutputStream` or `Serializable`. Instead, it uses low-level byte stream encoding:
- 4-byte Magic Number (`0x56415843` / "VAXC")
- 4-byte Record Count
- Each record's fields are manually packed via `java.nio.ByteBuffer` (4-byte integers and 4-byte length-prefixed UTF-8 byte arrays).
- Deserialization is protected by nested `try-catch-finally` blocks and verified against the SQLite record count.

---

## 📚 Academic Syllabus Mapping

For the complete academic syllabus mapping linking every topic in **Java Units I–IV** (Abstraction, Encapsulation, Dynamic Dispatch, Custom Exceptions, Multithreading, AWT Event Delegation, Byte Streams, Collections Framework, and JDBC) directly to specific files and line numbers:

👉 **[SYLLABUS_MAPPING_TABLE.md](SYLLABUS_MAPPING_TABLE.md)**

---

## 📄 License
This project is licensed under the MIT License.
