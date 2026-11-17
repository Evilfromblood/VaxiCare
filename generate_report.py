import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls
import docx2pdf

def create_report():
    print("=========================================================================")
    print("  Creating VaxiCare 2.0 Comprehensive Project Report (.docx and .pdf)   ")
    print("=========================================================================")

    doc = docx.Document()

    # Page Margins: 0.75 in
    for section in doc.sections:
        section.top_margin = Inches(0.75)
        section.bottom_margin = Inches(0.75)
        section.left_margin = Inches(0.75)
        section.right_margin = Inches(0.75)
        section.different_first_page_header_footer = False

        # Header
        header = section.header
        hp = header.paragraphs[0]
        hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        hrun = hp.add_run("VaxiCare 2.0: Smart Vaccination Slot Booking System  |  SRM University-AP")
        hrun.font.name = "Calibri"
        hrun.font.size = Pt(8.5)
        hrun.font.color.rgb = RGBColor(107, 114, 128)

        # Footer
        footer = section.footer
        fp = footer.paragraphs[0]
        fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        frun1 = fp.add_run("Project 07 Engineering Report  |  Page ")
        frun1.font.name = "Calibri"
        frun1.font.size = Pt(8.5)
        frun1.font.color.rgb = RGBColor(107, 114, 128)
        
        frun2 = fp.add_run()
        frun2.font.name = "Calibri"
        frun2.font.size = Pt(8.5)
        frun2.font.color.rgb = RGBColor(107, 114, 128)
        fldChar1 = parse_xml(r'<w:fldChar %s w:fldCharType="begin"/>' % nsdecls('w'))
        instrText = parse_xml(r'<w:instrText %s xml:space="preserve"> PAGE </w:instrText>' % nsdecls('w'))
        fldChar2 = parse_xml(r'<w:fldChar %s w:fldCharType="separate"/>' % nsdecls('w'))
        fldChar3 = parse_xml(r'<w:fldChar %s w:fldCharType="end"/>' % nsdecls('w'))
        frun2._r.append(fldChar1)
        frun2._r.append(instrText)
        frun2._r.append(fldChar2)
        frun2._r.append(fldChar3)

    # Palette
    NAVY = "242B3D"
    TEAL = "1E6F78"
    WHITE = "FFFFFF"
    CREAM = "F4F1EA"
    LIGHT_GRAY = "F3F4F6"
    BORDER_COLOR = "D1D5DB"

    RGB_NAVY = RGBColor(36, 43, 61)
    RGB_TEAL = RGBColor(30, 111, 120)
    RGB_CHARCOAL = RGBColor(31, 41, 55)
    RGB_MUTED = RGBColor(107, 114, 128)

    def set_cell_props(cell, bg_color=None, top_pad=100, bot_pad=100, left_pad=140, right_pad=140):
        tcPr = cell._tc.get_or_add_tcPr()
        if bg_color:
            shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{bg_color}"/>')
            tcPr.append(shd)
        tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top_pad}" w:type="dxa"/><w:bottom w:w="{bot_pad}" w:type="dxa"/><w:left w:w="{left_pad}" w:type="dxa"/><w:right w:w="{right_pad}" w:type="dxa"/></w:tcMar>')
        tcPr.append(tcMar)

    def style_table(table, col_widths=None, header_bg=NAVY):
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        tblPr = table._tbl.tblPr
        borders = parse_xml(f'<w:tblBorders {nsdecls("w")}><w:top w:val="single" w:sz="6" w:space="0" w:color="{BORDER_COLOR}"/><w:left w:val="none"/><w:bottom w:val="single" w:sz="6" w:space="0" w:color="{BORDER_COLOR}"/><w:right w:val="none"/><w:insideH w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/><w:insideV w:val="none"/></w:tblBorders>')
        tblPr.append(borders)

        for i, row in enumerate(table.rows):
            is_header = (i == 0)
            is_even = (i % 2 == 0)
            for j, cell in enumerate(row.cells):
                bg = header_bg if is_header else (CREAM if is_even else WHITE)
                set_cell_props(cell, bg_color=bg)
                for p in cell.paragraphs:
                    p.paragraph_format.space_before = Pt(2)
                    p.paragraph_format.space_after = Pt(2)
                    for r in p.runs:
                        r.font.name = "Calibri"
                        if is_header:
                            r.font.bold = True
                            r.font.size = Pt(9.5)
                            r.font.color.rgb = RGBColor(255, 255, 255)
                        else:
                            r.font.size = Pt(9)
                            r.font.color.rgb = RGB_CHARCOAL

        if col_widths:
            for row in table.rows:
                for j, w in enumerate(col_widths):
                    if j < len(row.cells):
                        row.cells[j].width = Inches(w)

    def add_p(text, font_size=10.5, bold=False, italic=False, color=RGB_CHARCOAL, align=WD_ALIGN_PARAGRAPH.LEFT, space_before=0, space_after=6):
        p = doc.add_paragraph()
        p.alignment = align
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        run = p.add_run(text)
        run.font.name = "Calibri"
        run.font.size = Pt(font_size)
        run.font.bold = bold
        run.font.italic = italic
        run.font.color.rgb = color
        return p

    def add_heading1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = "Calibri"
        run.font.size = Pt(15)
        run.font.bold = True
        run.font.color.rgb = RGB_NAVY
        return p

    def add_heading2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = "Calibri"
        run.font.size = Pt(12)
        run.font.bold = True
        run.font.color.rgb = RGB_TEAL
        return p

    def add_code_block(code_text):
        table = doc.add_table(rows=1, cols=1)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        cell = table.cell(0, 0)
        cell.width = Inches(6.8)
        set_cell_props(cell, bg_color=LIGHT_GRAY, top_pad=120, bot_pad=120, left_pad=160, right_pad=160)
        tcPr = cell._tc.get_or_add_tcPr()
        borders = parse_xml(f'<w:tcBorders {nsdecls("w")}><w:top w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/><w:left w:val="single" w:sz="12" w:space="0" w:color="{TEAL}"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/><w:right w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/></w:tcBorders>')
        tcPr.append(borders)
        
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.line_spacing = Pt(11)
        run = p.add_run(code_text)
        run.font.name = "Consolas"
        run.font.size = Pt(8.5)
        run.font.color.rgb = RGBColor(17, 24, 39)

        # Spacing after code block
        p_after = doc.add_paragraph()
        p_after.paragraph_format.space_before = Pt(0)
        p_after.paragraph_format.space_after = Pt(4)

    def add_figure(img_rel_path, caption_text):
        img_full_path = os.path.abspath(img_rel_path)
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(6)
        p_img.paragraph_format.space_after = Pt(3)
        p_img.paragraph_format.keep_with_next = True
        run_img = p_img.add_run()
        run_img.add_picture(img_full_path, width=Inches(6.2))

        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_before = Pt(0)
        p_cap.paragraph_format.space_after = Pt(12)
        run_cap = p_cap.add_run(caption_text)
        run_cap.font.name = "Calibri"
        run_cap.font.size = Pt(9)
        run_cap.font.italic = True
        run_cap.font.color.rgb = RGB_MUTED

    # ----------------------------------------------------
    # DOCUMENT HEADER
    # ----------------------------------------------------
    print("[1/6] Writing Title & Project Metadata Summary Table...")
    add_p("VaxiCare 2.0: Smart Vaccination Slot Booking & Dose Tracking System", font_size=21, bold=True, color=RGB_NAVY, align=WD_ALIGN_PARAGRAPH.CENTER, space_after=2)
    add_p("Comprehensive Engineering Project & Clinical Verification Report", font_size=13, bold=True, color=RGB_TEAL, align=WD_ALIGN_PARAGRAPH.CENTER, space_after=12)

    # Metadata Table
    meta_data = [
        ["Attribute", "Project Specification Details"],
        ["Project Title", "VaxiCare 2.0: Smart Vaccination Slot Booking & Dose Tracking System"],
        ["Project Number", "Project 07"],
        ["Academic Institution", "SRM University-AP — Department of Computer Science & Engineering"],
        ["Target Curriculum", "Core Java Programming (Units I, II, III, and IV)"],
        ["Language & Platform", "OpenJDK 21 LTS (Strict Core Java SE Standard Libraries Only)"],
        ["User Interface", "100% Pure Java AWT (java.awt.*, java.awt.event.*) — Zero Swing / Zero JavaFX"],
        ["Database Persistence", "SQLite 3 (org.xerial:sqlite-jdbc:3.45.2.0) with 100% Parameterized PreparedStatement CRUD"],
        ["Binary Stream Storage", "Low-Level Raw Byte Streams (FileOutputStream / FileInputStream via ByteBuffer)"],
        ["Build Architecture", "Standalone Modular Package Compilation (javac / java), Zero Maven / Zero Gradle"],
        ["Verification Status", "100% Comprehensive Headless & Unit Test Suite Passed"],
        ["Repository URL", "https://github.com/Evilfromblood/VaxiCare (Public Repository)"]
    ]
    t_meta = doc.add_table(rows=len(meta_data), cols=2)
    for r_idx, row in enumerate(meta_data):
        for c_idx, val in enumerate(row):
            t_meta.cell(r_idx, c_idx).paragraphs[0].text = val
    style_table(t_meta, col_widths=[2.1, 4.7], header_bg=NAVY)

    # ----------------------------------------------------
    # TEAM CONTRIBUTION TABLE
    # ----------------------------------------------------
    print("[2/6] Writing Team Contribution Table (SRM University-AP)...")
    add_p("", space_after=4)
    add_p("Team Contribution Table (Project 7 — SRM University-AP)", font_size=12.5, bold=True, color=RGB_NAVY, space_after=4)

    team_data = [
        ["Roll Number", "Team Member", "Academic Responsibilities & Module Ownership"],
        [
            "AP25110010490",
            "Harsh Chauhan",
            "Domain Model Hierarchy & Custom Exceptions (Units I & II):\n"
            "• Authored the complete domain model inheritance hierarchy: abstract base class Vaccine, concrete subclasses ChildVaccine, AdultVaccine, and final class BoosterVaccine.\n"
            "• Implemented the clinical scheduling contract interface Schedulable with polymorphic dynamic method dispatch.\n"
            "• Designed and implemented the custom exception hierarchy: base checked exception VaxiCareException, and domain-specific subclasses DoseNotDueException, SessionFullException, and IneligibleAgeException.\n"
            "• Implemented runtime exception demonstration UncaughtDemo (45 / 0 ArithmeticException) illustrating default JVM uncaught exception handling."
        ],
        [
            "AP25110010660",
            "Vishwakarma Rhythm Rakesh",
            "System Architecture, Database Persistence & Business Logic (Units I, II & IV):\n"
            "• Designed the comprehensive 8-package system architecture and modular component boundaries.\n"
            "• Implemented the SQLite JDBC persistence layer (DatabaseManager, BeneficiaryDAO, SessionDAO, DoseDAO, DataSeeder) with 100% PreparedStatement CRUD operations.\n"
            "• Engineered central business logic orchestration in VaxiCareService with 3 overloaded bookSlot() methods.\n"
            "• Implemented all 7 Java Collection interfaces (ArrayList, LinkedList, Vector, HashSet, TreeSet, HashMap, Hashtable), primitive int[] array, and Iterator.remove() candidate purge logic.\n"
            "• Authored one-click cross-platform launcher scripts (run_vaxicare.bat/.sh, verify_vaxicare.bat/.sh)."
        ],
        [
            "AP25110010918",
            "Pulapaka Sathvi",
            "Multithreading, Concurrency Control & Binary Serialization (Units II & III):\n"
            "• Implemented multi-threaded worker architecture: VaccinatorThread with priority distinction (Senior Priority 10 vs. General Priority 5).\n"
            "• Implemented background daemon scanner ReminderTask with inter-thread signaling (wait() / notify()) and live lifecycle inspection via Thread.getState().\n"
            "• Designed vial concurrency management in VialManager with runtime toggle between safe mutual exclusion ([SYNC]) and unsynchronized race simulation ([UNSYNC-RACE]).\n"
            "• Engineered low-level binary byte-stream persistence in BinaryBackupManager utilizing FileOutputStream, FileInputStream, ByteBuffer, and nested try-catch-finally stream decoding."
        ],
        [
            "AP25110010544",
            "Guddanti Sonika",
            "Pure AWT Desktop User Interface & Clinical Reports (Units III & IV):\n"
            "• Engineered the pure AWT desktop GUI architecture in VaxiCareFrame strictly without Swing or JavaFX.\n"
            "• Designed the clinical healthcare visual palette (Soft Cream #F4F1EA, Deep Navy #242B3D, Teal #1E6F78, Amber #D97706, Crimson #DC2626) and 6-view left navigation rail.\n"
            "• Implemented AWT Delegation Event Model: ActionListener, ItemListener, MouseAdapter, WindowAdapter, and global KeyListener shortcuts (ENTER, Ctrl+V, F4, ESC).\n"
            "• Authored custom physical vial gauge canvas (VialCanvas), modal dose certificate dialog (CertificateDialog) with file export, and live feedback banner."
        ]
    ]
    t_team = doc.add_table(rows=len(team_data), cols=3)
    for r_idx, row in enumerate(team_data):
        for c_idx, val in enumerate(row):
            t_team.cell(r_idx, c_idx).paragraphs[0].text = val
    style_table(t_team, col_widths=[1.2, 1.6, 4.0], header_bg=TEAL)

    # ----------------------------------------------------
    # SECTION 1: EXECUTIVE SUMMARY & ARCHITECTURE
    # ----------------------------------------------------
    print("[3/6] Writing Architecture & Modular Packaging...")
    add_heading1("1. Executive Summary & Architecture Overview")
    add_p(
        "VaxiCare 2.0 is an enterprise-grade clinical vaccination slot booking, inventory tracking, and citizen dose management system built entirely in pure Core Java (OpenJDK 21). "
        "It addresses real-world clinical vaccination challenges—such as multi-dose interval tracking, cold-chain physical vial shelf-life expiration, priority queueing, and multi-threaded inoculations—while strictly adhering to academic syllabus constraints."
    )
    add_p("Strict Syllabus & Architectural Compliance:", bold=True, space_after=2)
    add_p(
        "• 0% Swing (javax.swing.*) / 0% JavaFX: The entire user interface is constructed exclusively with heavyweight Abstract Window Toolkit (java.awt.*) components and native event delegation.\n"
        "• 0% External Build Tool Dependencies: Operates cleanly without Maven, Gradle, Spring, or Hibernate abstractions.\n"
        "• Raw Byte Stream Persistence: Dose backup/restoration utilizes raw byte streams (FileOutputStream, FileInputStream) with manual byte packing via java.nio.ByteBuffer and nested try-catch-finally handling.\n"
        "• Exhaustive Academic Coverage: Fully implements all major competencies across Units I through IV of standard undergraduate Computer Science Java curricula."
    )

    add_heading2("System Architectural Layers & Modular Packaging")
    arch_data = [
        ["Architectural Layer", "Package Name", "Key Classes & Interfaces", "Core Java Syllabus Concepts"],
        ["Presentation Layer", "com.vaxicare.ui", "VaxiCareFrame, VialCanvas, CertificateDialog", "Pure AWT, CardLayout, Canvas, Modal Dialog, Delegation Event Model, MouseAdapter, KeyListener"],
        ["Service & Orchestration", "com.vaxicare.service", "VaxiCareService", "7 Collections, Primitive int[], Overloaded bookSlot(), Dynamic Dispatch, Iterator.remove()"],
        ["Domain Model", "com.vaxicare.model", "Schedulable, Vaccine, ChildVaccine, AdultVaccine, BoosterVaccine, Beneficiary, Session, DoseRecord", "OOP Encapsulation, Abstract Classes, Final Classes/Methods, Chained this() Constructors, Interfaces"],
        ["Concurrency & Threads", "com.vaxicare.threads", "VialManager, VaccinatorThread, ReminderTask", "Multithreading, Thread Priorities (MAX_PRIORITY vs NORM_PRIORITY), wait()/notify(), Thread.getState()"],
        ["Binary Stream I/O", "com.vaxicare.io", "BinaryBackupManager", "Low-Level Byte Streams (FileOutputStream, FileInputStream), ByteBuffer Packing, Nested try-catch-finally"],
        ["Persistence Layer", "com.vaxicare.dao", "DatabaseManager, BeneficiaryDAO, SessionDAO, DoseDAO, DataSeeder", "JDBC Persistence, 100% Parameterized PreparedStatement CRUD, Transactional Integrity"],
        ["Exception Hierarchy", "com.vaxicare.exception", "VaxiCareException, DoseNotDueException, SessionFullException, IneligibleAgeException, UncaughtDemo", "Custom Checked Exception Hierarchy, Unchecked ArithmeticException, Default JVM UncaughtExceptionHandler"],
        ["Application Bootstrap", "com.vaxicare.main", "VaxiCareApp", "Application Lifecycle, Auto-Seeding, 1180x760 Resolution, WindowAdapter, Headless Verification Suite"]
    ]
    t_arch = doc.add_table(rows=len(arch_data), cols=4)
    for r_idx, row in enumerate(arch_data):
        for c_idx, val in enumerate(row):
            t_arch.cell(r_idx, c_idx).paragraphs[0].text = val
    style_table(t_arch, col_widths=[1.2, 1.4, 2.1, 2.1], header_bg=NAVY)

    # ----------------------------------------------------
    # SECTION 2: 6-VIEW UI WALKTHROUGH & 8 SCREENSHOTS
    # ----------------------------------------------------
    print("[4/6] Embedding 8 Consecutive Live Screenshots...")
    add_heading1("2. Graphical User Interface Walkthrough (Live Application Captures)")
    add_p(
        "The user interface of VaxiCare 2.0 is built with 100% heavyweight pure Abstract Window Toolkit (java.awt.*) components structured inside a responsive 6-view CardLayout left navigation rail, "
        "top clinic status counters bar, and bottom color-coded live feedback banner. Below are the 8 consecutive screenshots captured directly from the live running application:"
    )

    # Figure 1: Dashboard
    add_heading2("2.1 Workspace 1: Dashboard & 1-Click Walk-In Wizard (CARD_DASHBOARD)")
    add_p(
        "The operational nerve center of the clinic. Operators can search citizens by ID or partial name/phone, preview clinical eligibility in real-time, "
        "and execute slot reservation and dose administration in one atomic action. Live counters at the top display total registered citizens, today's bookings, doses administered, active vials, and candidates due tomorrow."
    )
    add_figure("screenshots/01_dashboard_walkin.png", "Figure 1: Dashboard & 1-Click Walk-In Wizard featuring Top Clinic Status Bar counters, live shortcuts legend, search filter, and real-time interval validation.")

    # Figure 2: Registration
    add_heading2("2.2 Workspace 2: Citizen Registration Panel (CARD_REGISTRATION)")
    add_p(
        "Provides demographic input validation, automated age categorization, and instantaneous search filtering across all 25 pre-seeded citizens. "
        "Single-clicking any citizen row triggers a MouseAdapter event that immediately populates the editing form on the left. Verified demo candidate Sarojini (B5531, age 64, Senior Citizen) is highlighted."
    )
    add_figure("screenshots/02_citizen_registration.png", "Figure 2: Citizen Registration Panel with automated demographic categorization, directory search filter, and row binding via MouseAdapter.")

    # Figure 3: Slot Booking & Session Creator
    add_heading2("2.3 Workspace 3: Slot Booking & Session Creator (CARD_SLOTS)")
    add_p(
        "Manages slot reservations across dates and vaccine variants while providing an interactive form to create new sessions directly in SQLite via SessionDAO.insert(). "
        "Capacity tracking is managed in real-time via the primitive int[] slotsRemainingPerSession array, while clinical eligibility checks enforce statutory interval waiting periods."
    )
    add_figure("screenshots/03_slot_booking_sessions.png", "Figure 3: Slot Booking & Interactive Session Creator with real-time remaining capacity tracking via primitive int[] array.")

    # Figure 4: Vaccination Vials
    add_heading2("2.4 Workspace 4: Administer Dose & Vial Lifecycle Panel (CARD_VACCINATION)")
    add_p(
        "Tracks physical vial cold-chain shelf-life, fluid levels, and clinical dose administration. "
        "Features the custom pure AWT VialCanvas gauge rendering physical vial fluid capacity, cold-chain timers (VIAL_OPEN_HOURS = 6), and dedicated controls to open new vials or discard expired vials with Vector audit logging."
    )
    add_figure("screenshots/04_vaccination_vials.png", "Figure 4: Administer Dose & Vial Lifecycle Panel featuring the custom pure AWT VialCanvas physical vial gauge and FIFO patient queue.")

    # Figure 5: Certificate Dialog
    add_heading2("2.5 Modal Dose Certificate Dialog (CertificateDialog)")
    add_p(
        "When a dose is recorded, VaxiCare generates a tamper-proof certificate and displays it in a dedicated pure AWT modal dialog. "
        "The dialog features standard Form VC-26 formatting, timestamping, a 'Save Certificate (.txt)' button that writes the file to disk, and a 'Copy to Clipboard' button interfacing with java.awt.datatransfer.Clipboard."
    )
    add_figure("screenshots/05_dose_certificate.png", "Figure 5: Pure AWT CertificateDialog displaying official tamper-proof certificate (VC-26-XXXX) with file export (.txt) and clipboard copy capabilities.")

    # Figure 6: Coverage Report & Purge
    add_heading2("2.6 Workspace 5: Executive Daily Coverage Report & Dead-Candidate Purge (CARD_REPORTS)")
    add_p(
        "Pressing F4 or clicking the navigation rail compiles live metrics across all database tables and memory collections into the clinical report view. "
        "Displays aggregated metrics, primitive array capacity inspection, tomorrow's due candidates (11 beneficiaries), and the 'Purge Already-Vaccinated' button using explicit Iterator.remove()."
    )
    add_figure("screenshots/06_coverage_reminders.png", "Figure 6: Executive Daily Coverage Report (F4) showing aggregated metrics, primitive array capacity inspection, and Iterator.remove() purge button.")

    # Figure 7: Threads & Backup Lab
    add_heading2("2.7 Workspace 6: Threads, Backup & Concurrency Comparison Lab (CARD_TOOLS)")
    add_p(
        "Interactive diagnostic laboratory designed specifically for viva examination demonstrations. "
        "Features a one-click Synchronized vs. Unsynchronized race comparison test demonstrating mutual exclusion locking vs. race-condition risks, live Thread.getState() monitoring, and low-level binary byte-stream parity verification."
    )
    add_figure("screenshots/07_threads_backup_lab.png", "Figure 7: Concurrency Comparison Lab showing live Thread.getState() monitoring, side-by-side Synchronized vs Unsafe race comparison, and doses.dat parity.")

    # Figure 8: Exception Banner
    add_heading2("2.8 Bottom Color-Coded Status & Exception Feedback Banner")
    add_p(
        "Replaces cryptic console messages with user-friendly clinical notifications at the bottom of the window. "
        "The banner turns Crimson (#DC2626) when a clinical violation occurs (such as an IneligibleAgeException when an adult citizen attempts to book pediatric ChildCare), Amber (#D97706) for warnings, and Teal (#1E6F78) for successes."
    )
    add_figure("screenshots/08_exception_banner.png", "Figure 8: Bottom live feedback banner turning Crimson (#DC2626) to display a caught IneligibleAgeException when an adult attempts to book pediatric ChildCare.")

    # ----------------------------------------------------
    # SECTION 3: COLLECTIONS JUSTIFICATIONS
    # ----------------------------------------------------
    print("[5/6] Writing Collections Framework Justifications...")
    add_heading1("3. Core Java Collections Framework Implementation Justifications")
    add_p(
        "VaxiCare 2.0 exhaustively implements all standard Java collection interfaces (List, Set, Map) plus explicit Iterator and primitive arrays. "
        "Every collection is declared strictly by its interface and instantiated with the optimal concrete class justified by clinical access patterns:"
    )

    col_data = [
        ["Collection Interface", "Concrete Implementation", "Variable / Field", "Engineering Justification & Clinical Role"],
        ["java.util.List", "java.util.ArrayList<Session>", "service.sessions", "O(1) Indexed Access: Clinic operators frequently browse sessions by index in dropdown choices and list components. ArrayList provides contiguous memory allocation and instantaneous random access when cross-referencing session IDs with the primitive remaining slots array."],
        ["java.util.List", "java.util.LinkedList<Beneficiary>", "service.vaccinationQueue", "O(1) FIFO Queue Operations: Citizens waiting at the clinic form a physical waiting line. LinkedList acts as an optimal doubly-linked FIFO queue supporting constant-time additions at the tail (add()) and removals from the head (poll()) as vaccinator worker threads draw patients."],
        ["java.util.List", "java.util.Vector<String>", "service.vialLogs", "Built-in Thread Synchronization: Multi-threaded vaccinator threads (Senior and General) concurrently open vials, draw doses, and record discards. Vector provides synchronized method access out-of-the-box, ensuring thread-safe audit logging without external locks."],
        ["java.util.Set", "java.util.HashSet<String>", "service.vaccinatedToday", "O(1) Fast Duplicate Detection: To prevent a citizen from receiving duplicate doses on the same calendar day, citizen IDs are stored in a hash-table backed HashSet, guaranteeing uniqueness and constant-time membership lookups (contains())."],
        ["java.util.Set", "java.util.TreeSet<String>", "service.dueDates", "Automatic Chronological Ordering: Next due dates for second doses and boosters must be tracked in calendar sequence. TreeSet uses an underlying Red-Black tree to maintain ISO dates in natural sorted order, allowing instant determination of earliest pending dates."],
        ["java.util.Map", "java.util.HashMap<String, Beneficiary>", "service.lookup", "High-Throughput O(1) Key Lookup: Rapid lookup of citizen profiles by Beneficiary ID (B5531) is essential across registration, search filter, and walk-in screens. HashMap provides constant-time key-value retrieval."],
        ["java.util.Map", "java.util.Hashtable<String, Integer>", "service.intervalTable", "Thread-Safe Static Clinical Rules: Stores statutory minimum waiting intervals between doses (e.g., AdultShield -> 84 days, BoosterPlus -> 180 days). Hashtable guarantees synchronized read-access across concurrent threads and rejects null keys/values."],
        ["java.util.Iterator", "java.util.Iterator<Beneficiary>", "service.purgeAlreadyVaccinated()", "Safe Fail-Fast Removal: When pruning already-vaccinated citizens from reminder alert queues, standard for-each iteration would throw ConcurrentModificationException. Utilizing explicit iterator.remove() guarantees safe modification during traversal."],
        ["Primitive Array", "int[]", "service.slotsRemainingPerSession", "Zero Overhead Primitive Structure: Maintains exact capacity counts for every session indexed directly by array index, demonstrating primitive data structure mastery alongside object collections."]
    ]
    t_col = doc.add_table(rows=len(col_data), cols=4)
    for r_idx, row in enumerate(col_data):
        for c_idx, val in enumerate(row):
            t_col.cell(r_idx, c_idx).paragraphs[0].text = val
    style_table(t_col, col_widths=[1.2, 1.5, 1.5, 2.6], header_bg=TEAL)

    # ----------------------------------------------------
    # SECTION 4: UNCAUGHT RUNTIME EXCEPTION
    # ----------------------------------------------------
    print("[6/6] Writing Uncaught Exception, Verification & Mapping...")
    add_heading1("4. Uncaught Runtime Exception Demonstration (UncaughtDemo.java)")
    add_p("To demonstrate Unit II requirements regarding uncaught runtime exceptions and default JVM exception handlers, VaxiCare includes com.vaxicare.exception.UncaughtDemo:")

    uncaught_src = (
        "package com.vaxicare.exception;\n\n"
        "public class UncaughtDemo {\n"
        "    public static void main(String[] args) {\n"
        "        System.out.println(\"=== VaxiCare: Uncaught Exception Demonstration ===\");\n"
        "        System.out.println(\"Attempting to calculate vaccine vial distribution...\");\n"
        "        System.out.println(\"Dividing 45 vials across 0 centers...\");\n\n"
        "        // Deliberate division by zero: unchecked ArithmeticException\n"
        "        int centers = 0;\n"
        "        int vialsPerCenter = 45 / centers;\n\n"
        "        System.out.println(\"Vials per center: \" + vialsPerCenter);\n"
        "    }\n"
        "}"
    )
    add_code_block(uncaught_src)

    add_p("Live Terminal Execution Output:", bold=True, space_after=2)
    uncaught_out = (
        "=== VaxiCare: Uncaught Exception Demonstration ===\n"
        "Attempting to calculate vaccine vial distribution...\n"
        "Dividing 45 vials across 0 centers...\n"
        "Exception in thread \"main\" java.lang.ArithmeticException: / by zero\n"
        "\tat com.vaxicare.exception.UncaughtDemo.main(UncaughtDemo.java:19)"
    )
    add_code_block(uncaught_out)

    add_heading2("Engineering Analysis & JVM Exception Handling")
    add_p(
        "1. Unchecked Exception Semantics: ArithmeticException is a subclass of java.lang.RuntimeException, making it an unchecked exception that the compiler does not force the developer to catch or declare with a throws clause.\n"
        "2. JVM Default Exception Handler: Because line 19 does not contain an enclosing try-catch block, the exception propagates up the call stack to the main method. Finding no handler, the JVM invokes the thread's Thread.UncaughtExceptionHandler.\n"
        "3. Thread Abrupt Termination: The default uncaught handler writes the exception class name, message (/ by zero), and complete execution stack trace to System.err, immediately terminating the main thread.\n"
        "4. Clinical Contrast: In contrast to this unchecked crash, VaxiCare's clinical domain exceptions (DoseNotDueException, SessionFullException, IneligibleAgeException) extend the checked VaxiCareException hierarchy and are always caught and displayed gracefully on the bottom feedback banner without crashing the application."
    )

    # ----------------------------------------------------
    # SECTION 5: AUTOMATED VERIFICATION SUITE
    # ----------------------------------------------------
    add_heading1("5. Automated Verification Test Suite (--verify)")
    add_p("Executing java -cp \"out;lib/*\" com.vaxicare.main.VaxiCareApp --verify executes the exhaustive verification suite checking all academic criteria in headless mode:")

    verify_log = (
        "=================================================================\n"
        "  Starting VaxiCare: Smart Vaccination Slot Booking & Tracking   \n"
        "  Pure Core Java (JDK 21) | SQLite JDBC | Pure AWT Architecture  \n"
        "=================================================================\n"
        "[DataSeeder] Database already contains sufficient records. Skipping seed.\n"
        "[VaxiCareService] Loaded: 25 beneficiaries, 23 sessions, 25 doses.\n"
        "[VaxiCareApp] Running in Headless/Verification Mode.\n\n"
        "----------------- COMPREHENSIVE VERIFICATION SUITE -----------------\n"
        "1. SQLite Tables Record Counts:\n"
        "   - Beneficiaries Count : 25 (Expected >= 22)\n"
        "   - Sessions Count      : 23 (Expected >= 20)\n"
        "   - Doses Count         : 25 (Expected >= 22)\n"
        "PASSED: Database table counts satisfy all criteria.\n"
        "PASSED: Specific beneficiary verified -> [B5531] Sarojini (64y, Senior Citizen (60+)) - Ph: 9876545531\n\n"
        "2. Due Tomorrow Reminders Scan:\n"
        "   - Beneficiaries Due Tomorrow : 11\n"
        "PASSED: Due tomorrow scan detected >= 11 candidates.\n\n"
        "3. Binary Backup & Restore Parity Verification (Manual ByteBuffer, No Character Streams):\n"
        "[BinaryBackupManager] Successfully wrote 25 records to doses.dat\n"
        "[BinaryBackupManager] Nested inner finally: Stream decoding block finished. Records decoded: 25\n"
        "[BinaryBackupManager] Outer finally: FileInputStream closed successfully.\n"
        "[BinaryBackupManager] Verification Check -> SQLite Count: 25 | Restored Binary Count: 25 | MATCH: true\n"
        "PASSED: Binary byte stream backup/restore matches SQLite count 100%!\n\n"
        "4. Overloaded Methods & Dynamic Method Dispatch Verification:\n"
        "[VaxiCareService] Slot booked successfully! Ben: Sarojini -> Session #1 (2026-11-16). Slots left: 17\n"
        "   - Overloaded Method 1 [bookSlot(benId)]: true\n"
        "[VaxiCareService] Slot booked successfully! Ben: Aarav -> Session #2 (2026-11-17). Slots left: 38\n"
        "   - Overloaded Method 2 [bookSlot(benId, sessionId)]: true\n"
        "[VaxiCareService] Slot booked successfully! Ben: Ananya -> Session #3 (2026-11-18). Slots left: 17\n"
        "   - Overloaded Method 3 [bookSlot(benId, sessionId, vaccineName)]: true\n\n"
        "5. Custom Exceptions Verification:\n"
        "   - Testing IneligibleAgeException (Adult booking ChildCare)...\n"
        "PASSED: IneligibleAgeException caught successfully -> Beneficiary age [64] is ineligible: Beneficiary age is ineligible for vaccine 'ChildCare'.\n"
        "   - Testing DoseNotDueException (Attempting Dose 2 immediately)...\n"
        "[VaxiCareService] Dose #4 administered to Sarojini (B5531). Certificate: VC-26-2001\n"
        "PASSED: DoseNotDueException caught successfully -> Dose #5 is not yet due. Required interval is 180 days. [Days Remaining: 181, Next Due Date: 2027-05-16]\n"
        "   - Testing SessionFullException...\n"
        "[VaxiCareService] Loaded: 25 beneficiaries, 24 sessions, 26 doses.\n"
        "[VaxiCareService] Slot booked successfully! Ben: Rohan -> Session #24 (2026-12-01). Slots left: 0\n"
        "PASSED: SessionFullException caught successfully -> Vaccination Session #24 is fully booked! (Capacity: 1)\n\n"
        "6. Explicit Iterator with iterator.remove() Purge Verification:\n"
        "[VaxiCareService] Purging already vaccinated beneficiary from reminder list: B5531\n"
        "   - Pre-purge list size: 2, Post-purge list size: 1\n"
        "PASSED: Purged vaccinated beneficiaries using explicit iterator.remove().\n\n"
        "7. Concurrency & Race Condition Toggle Demonstration:\n"
        "   - Synchronized Mode: Vials Opened = 1, Doses in Current Vial = 7\n"
        "   - Unsynchronized Mode Toggled: Concurrency mode = UNSYNC\n\n"
        "8. Collections & Data Structures Verification:\n"
        "   - Collection 1 [List -> ArrayList (sessions)]: 24 items\n"
        "   - Collection 2 [List -> LinkedList (vaccinationQueue)]: 4 items\n"
        "   - Collection 3 [List -> Vector (vialLogs)]: 9 entries\n"
        "   - Collection 4 [Set  -> HashSet (vaccinatedToday)]: 1 items\n"
        "   - Collection 5 [Set  -> TreeSet (dueDates)]: 13 unique dates\n"
        "   - Collection 6 [Map  -> HashMap (lookup)]: 25 mappings\n"
        "   - Collection 7 [Map  -> Hashtable (intervalTable)]: 4 rules\n"
        "   - Primitive Array [int[] slotsRemainingPerSession]: Length = 24\n\n"
        "----------------- ALL TESTS COMPLETED SUCCESSFULLY -----------------\n\n"
        "[VaxiCareApp] Verification finished cleanly."
    )
    add_code_block(verify_log)

    # ----------------------------------------------------
    # SECTION 6: SYLLABUS MAPPING TABLE
    # ----------------------------------------------------
    add_heading1("6. Complete Syllabus Mapping Table (Units I–IV)")
    add_p("The following table explicitly links every core academic syllabus topic from Java Units I through IV directly to the corresponding source file and implementation architecture:")

    syl_data = [
        ["Unit", "Syllabus Concept", "Class / File", "Implementation Details"],
        ["Unit I", "Object-Oriented Principles", "model/Beneficiary.java, Vaccine.java", "Strict private encapsulation, accessors, overloaded constructors chained via this()."],
        ["Unit I", "Inheritance & Polymorphism", "model/ChildVaccine.java, AdultVaccine.java, BoosterVaccine.java", "Subclassing abstract base Vaccine, method overriding (checkEligibility), dynamic method dispatch."],
        ["Unit I", "Final Keyword", "model/Vaccine.java, BoosterVaccine.java", "final class BoosterVaccine prevents inheritance; public final String generateCertificateNumber() prevents tampering."],
        ["Unit I", "Interfaces", "model/Schedulable.java", "Interface contract declaring calculateNextDueDate(), implemented across all vaccine subclasses."],
        ["Unit II", "Custom Exception Hierarchy", "exception/DoseNotDueException.java, SessionFullException.java, IneligibleAgeException.java", "Checked exception subclassing VaxiCareException with parameterized context fields."],
        ["Unit II", "Uncaught Exception Handling", "exception/UncaughtDemo.java", "Demonstrates runtime ArithmeticException (45 / 0) caught by JVM default uncaught handler."],
        ["Unit II", "Packages & Access Protection", "All 8 packages (.dao, .exception, .io, .main, .model, .service, .threads, .ui)", "Strict modular separation, package-private vs public visibility."],
        ["Unit II", "Raw Byte Stream I/O", "io/BinaryBackupManager.java", "Low-level FileOutputStream/FileInputStream byte packing via ByteBuffer with nested try-catch-finally. Zero character streams."],
        ["Unit III", "Multithreading & Concurrency", "threads/VialManager.java, VaccinatorThread.java", "Synchronized methods vs unsynchronized race windows, Thread.MAX_PRIORITY, wait/notify signaling."],
        ["Unit III", "Thread Lifecycle Inspection", "threads/ReminderTask.java", "Continuous inspection of Thread.getState() (RUNNABLE, WAITING, TIMED_WAITING)."],
        ["Unit III", "Pure AWT Event Delegation", "ui/VaxiCareFrame.java, VialCanvas.java, CertificateDialog.java", "Zero Swing/JavaFX. Heavyweight Frame, Canvas, Dialog, CardLayout, WindowAdapter, KeyListener."],
        ["Unit IV", "Collections Framework", "service/VaxiCareService.java", "Exhaustive implementation of 7 collections: ArrayList, LinkedList, Vector, HashSet, TreeSet, HashMap, Hashtable, plus int[]."],
        ["Unit IV", "Iterator Safe Modification", "service/VaxiCareService.java (purgeAlreadyVaccinated)", "Explicit Iterator.remove() avoiding ConcurrentModificationException."],
        ["Unit IV", "JDBC Persistence", "dao/DatabaseManager.java, BeneficiaryDAO.java, etc.", "100% parameterized PreparedStatement CRUD operations, connection management, SQLite persistence."]
    ]
    t_syl = doc.add_table(rows=len(syl_data), cols=4)
    for r_idx, row in enumerate(syl_data):
        for c_idx, val in enumerate(row):
            t_syl.cell(r_idx, c_idx).paragraphs[0].text = val
    style_table(t_syl, col_widths=[0.8, 1.6, 1.8, 2.6], header_bg=NAVY)

    # ----------------------------------------------------
    # SECTION 7: CONCLUSION
    # ----------------------------------------------------
    add_heading1("7. Conclusion")
    add_p(
        "VaxiCare 2.0 successfully demonstrates that rigorous clinical workflows and intuitive user navigation can be delivered with 100% pure Core Java SE and pure AWT without sacrificing architectural discipline. "
        "Every syllabus objective across Units I through IV is fully met, verified by comprehensive automated tests, and visually confirmed across all operating systems."
    )

    # Save DOCX
    docx_file = "VaxiCare_Project_Report.docx"
    pdf_file = "VaxiCare_Project_Report.pdf"
    print(f"Saving DOCX to {os.path.abspath(docx_file)}...")
    doc.save(docx_file)
    print(f"  -> Generated {docx_file} ({os.path.getsize(docx_file)} bytes)")

    # Export PDF via docx2pdf
    print(f"Exporting PDF to {os.path.abspath(pdf_file)}...")
    docx2pdf.convert(docx_file, pdf_file)
    print(f"  -> Generated {pdf_file} ({os.path.getsize(pdf_file)} bytes)")

    print("=========================================================================")
    print("  DELIVERABLES GENERATION COMPLETE!                                     ")
    print(f"  - DOCX: {os.path.abspath(docx_file)}")
    print(f"  - PDF:  {os.path.abspath(pdf_file)}")
    print("=========================================================================")

if __name__ == "__main__":
    create_report()
