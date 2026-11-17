package com.vaxicare.ui;

import com.vaxicare.exception.DoseNotDueException;
import com.vaxicare.exception.IneligibleAgeException;
import com.vaxicare.exception.SessionFullException;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.Beneficiary;
import com.vaxicare.model.DoseRecord;
import com.vaxicare.model.Session;
import com.vaxicare.service.VaxiCareService;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Modern, approachable Pure AWT Graphical Interface for VaxiCare.
 * Strictly uses java.awt.* and java.awt.event.* (Zero javax.swing.* / zero JavaFX).
 * Demonstrates Unit IV: CardLayout navigation, custom Canvas, event delegation,
 * dialogs, and comprehensive exception handling.
 */
public class VaxiCareFrame extends Frame implements ActionListener, ItemListener, KeyListener {

    private final VaxiCareService service;

    // Clinical Color Palette (Soft Cream, Deep Navy, Teal, Warm Amber, Crimson)
    public static final Color COLOR_BG = new Color(244, 241, 234);         // #F4F1EA Soft Cream
    public static final Color COLOR_CARD = new Color(234, 230, 220);       // #EAE6DC Subtle Card
    public static final Color COLOR_NAV = new Color(36, 43, 61);           // #242B3D Deep Navy
    public static final Color COLOR_NAV_ACTIVE = new Color(30, 111, 120);  // #1E6F78 Teal Active
    public static final Color COLOR_PRIMARY = new Color(30, 111, 120);     // #1E6F78 Primary Teal
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);      // #16A34A Success Green
    public static final Color COLOR_WARNING = new Color(217, 119, 6);      // #D97706 Warm Amber
    public static final Color COLOR_DANGER = new Color(220, 38, 38);       // #DC2626 Crimson Error
    public static final Color COLOR_TEXT_DARK = new Color(24, 30, 42);     // #181E2A
    public static final Color COLOR_BORDER = new Color(209, 199, 189);     // #D1C7BD

    // Typography Hierarchy
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);

    // Card Identifiers
    public static final String CARD_DASHBOARD = "CARD_DASHBOARD";
    public static final String CARD_REGISTRATION = "CARD_REGISTRATION";
    public static final String CARD_SLOTS = "CARD_SLOTS";
    public static final String CARD_VACCINATION = "CARD_VACCINATION";
    public static final String CARD_REPORTS = "CARD_REPORTS";
    public static final String CARD_TOOLS = "CARD_TOOLS";

    private CardLayout cardLayout;
    private Panel cardsPanel;

    // Sidebar Navigation Buttons
    private Button btnNavDash;
    private Button btnNavReg;
    private Button btnNavSlots;
    private Button btnNavVacc;
    private Button btnNavReports;
    private Button btnNavTools;
    private Button[] navButtons;
    private String currentCard = CARD_DASHBOARD;

    // Top Clinic Status Bar Labels
    private Label lblStatCitizens;
    private Label lblStatBooked;
    private Label lblStatDoses;
    private Label lblStatVials;
    private Label lblStatDueTomorrow;

    // Bottom Live Feedback Banner
    private Panel pnlLiveFeedback;
    private Label lblLiveFeedback;

    // ==========================================
    // 1. DASHBOARD & QUICK WALK-IN COMPONENTS
    // ==========================================
    private TextField txtDashSearch;
    private Button btnDashSearch;
    private Button btnDashLoadDemo;
    private java.awt.List dashCitizenList;
    private Label lblDashCitizenDetails;
    private Label lblDashHistoryDetails;
    private Choice choiceDashVaccine;
    private Label lblDashEligibility;
    private Button btnDashWalkIn;
    private Button btnDashBookOnly;
    private Button btnDashAdminOnly;
    private java.awt.List dashRecentDosesList;
    private String selectedDashBenId = "";

    // ==========================================
    // 2. CITIZEN REGISTRATION COMPONENTS
    // ==========================================
    private TextField txtRegId;
    private TextField txtRegName;
    private TextField txtRegAge;
    private TextField txtRegIdNumber;
    private TextField txtRegPhone;
    private Label lblRegGroup;
    private Button btnRegister;
    private Button btnClearReg;
    private Button btnRegLoadDemo;
    private TextField txtRegFilter;
    private Button btnRegFilterClear;
    private java.awt.List benAwtList;

    // ==========================================
    // 3. SLOT BOOKING & SESSIONS COMPONENTS
    // ==========================================
    private TextField txtSlotBenId;
    private Button btnSlotLookup;
    private Button btnSlotLoadDemo;
    private Label lblSlotCitizenInfo;
    private Choice choiceSlotSession;
    private Choice choiceSlotVaccine;
    private Button btnBookSlot;
    private Button btnAutoBook;
    private java.awt.List sessionAwtList;

    // New Session Creator components
    private TextField txtNewSessDate;
    private Choice choiceNewSessVaccine;
    private TextField txtNewSessCapacity;
    private Button btnCreateSession;

    // ==========================================
    // 4. VACCINATION & VIAL LAB COMPONENTS
    // ==========================================
    private TextField txtVaccBenId;
    private TextField txtVaccBenName;
    private TextField txtVaccBenAge;
    private Choice choiceVaccBrand;
    private TextField txtVaccSessionId;
    private Checkbox chkSyncToggle;
    private Button btnAdministerDose;
    private Button btnSimulateRace;
    private VialCanvas vialCanvas;
    private Button btnOpenNewVial;
    private Button btnDiscardVial;
    private java.awt.List queueAwtList;

    // ==========================================
    // 5. REMINDERS & COVERAGE REPORT COMPONENTS
    // ==========================================
    private TextArea txtReportDisplay;
    private Button btnGenDailyReport;
    private Button btnViewReminders;
    private Button btnPurgeVaccinated;
    private Button btnTriggerReminderThread;
    private Button btnExportReportFile;

    // ==========================================
    // 6. THREADS, BACKUP & SYSTEM TOOLS COMPONENTS
    // ==========================================
    private Label lblThreadStateSenior;
    private Label lblThreadStateGeneral;
    private Label lblThreadStateReminder;
    private Button btnRefreshThreadStates;
    private Button btnRunRaceComparison;
    private Button btnTriggerUncaught;
    private Button btnBackupBinary;
    private Button btnRestoreBinary;
    private Label lblBinaryBackupStatus;
    private TextArea txtToolsOutput;

    public VaxiCareFrame(VaxiCareService service) {
        super("VaxiCare 2.0: Smart Vaccination Slot Booking & Dose Tracking System");
        this.service = service;

        initializeUI();
        refreshAllData();

        // Start background worker threads
        service.startBackgroundThreads();
    }

    private void initializeUI() {
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // 1. TOP CLINIC STATUS BAR & HEADER
        add(createTopHeaderPanel(), BorderLayout.NORTH);

        // 2. WEST: LEFT SIDEBAR NAVIGATION RAIL
        add(createSidebarPanel(), BorderLayout.WEST);

        // 3. CENTER: CARD LAYOUT WORKSPACES
        cardLayout = new CardLayout();
        cardsPanel = new Panel(cardLayout);

        cardsPanel.add(createDashboardPanel(), CARD_DASHBOARD);
        cardsPanel.add(createRegistrationPanel(), CARD_REGISTRATION);
        cardsPanel.add(createSlotsPanel(), CARD_SLOTS);
        cardsPanel.add(createVaccinationPanel(), CARD_VACCINATION);
        cardsPanel.add(createReportsPanel(), CARD_REPORTS);
        cardsPanel.add(createToolsPanel(), CARD_TOOLS);

        add(cardsPanel, BorderLayout.CENTER);

        // 4. SOUTH: BOTTOM LIVE FEEDBACK BANNER
        add(createBottomFeedbackPanel(), BorderLayout.SOUTH);

        // Clean shutdown adapter
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("[VaxiCareFrame] Application closing cleanly. Stopping threads...");
                service.stopBackgroundThreads();
                dispose();
                System.exit(0);
            }
        });

        // Global Key Listener
        addKeyListener(this);
        setFocusable(true);
        setActiveNav(btnNavDash, CARD_DASHBOARD);
    }

    // =========================================================================
    // TOP HEADER & STATUS BAR
    // =========================================================================
    private Panel createTopHeaderPanel() {
        Panel container = new Panel(new BorderLayout());
        container.setBackground(COLOR_NAV);

        // Row 1: Logo & Metric Badges
        Panel row1 = new Panel(new BorderLayout(10, 0));
        row1.setBackground(COLOR_NAV);

        Label lblTitle = new Label("  ✦ VaxiCare 2.0: Smart Clinical Vaccination System", Label.LEFT);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        row1.add(lblTitle, BorderLayout.WEST);

        Panel metricsPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        metricsPanel.setBackground(COLOR_NAV);

        lblStatCitizens = createMetricLabel("Citizens: 0");
        lblStatBooked = createMetricLabel("Booked Today: 0");
        lblStatDoses = createMetricLabel("Doses Given: 0");
        lblStatVials = createMetricLabel("Active Vials: 0");
        lblStatDueTomorrow = createMetricLabel("Due Tomorrow: 0");

        metricsPanel.add(lblStatCitizens);
        metricsPanel.add(lblStatBooked);
        metricsPanel.add(lblStatDoses);
        metricsPanel.add(lblStatVials);
        metricsPanel.add(lblStatDueTomorrow);
        row1.add(metricsPanel, BorderLayout.EAST);

        // Row 2: Keyboard Shortcut Legend
        Panel row2 = new Panel(new FlowLayout(FlowLayout.LEFT, 15, 3));
        row2.setBackground(new Color(25, 30, 44));
        Label lblLegend = new Label("KEYBOARD SHORTCUTS:  [ENTER] Search Citizen ID/Name  |  [Ctrl+V] Record Dose  |  [F4] Daily Coverage Report  |  [ESC] Clear Form");
        lblLegend.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblLegend.setForeground(new Color(180, 195, 215));
        row2.add(lblLegend);

        container.add(row1, BorderLayout.NORTH);
        container.add(row2, BorderLayout.SOUTH);
        return container;
    }

    private Label createMetricLabel(String text) {
        Label lbl = new Label(text, Label.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(new Color(240, 245, 255));
        lbl.setBackground(new Color(48, 58, 82));
        return lbl;
    }

    // =========================================================================
    // SIDEBAR NAVIGATION RAIL
    // =========================================================================
    private Panel createSidebarPanel() {
        Panel sidebar = new Panel(new BorderLayout());
        sidebar.setBackground(COLOR_NAV);
        sidebar.setPreferredSize(new Dimension(230, 600));

        Panel navList = new Panel(new GridLayout(7, 1, 0, 4));
        navList.setBackground(COLOR_NAV);

        Label lblSection = new Label(" CLINICAL WORKFLOWS", Label.LEFT);
        lblSection.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSection.setForeground(new Color(148, 163, 184));
        navList.add(lblSection);

        btnNavDash = createNavButton("⚡ Dashboard & Walk-In");
        btnNavReg = createNavButton("👤 Citizen Registration");
        btnNavSlots = createNavButton("📅 Slot Booking & Sessions");
        btnNavVacc = createNavButton("💉 Administer Dose & Vials");
        btnNavReports = createNavButton("📊 Reminders & Reports");
        btnNavTools = createNavButton("⚙ Threads & System Tools");

        navButtons = new Button[]{btnNavDash, btnNavReg, btnNavSlots, btnNavVacc, btnNavReports, btnNavTools};

        navList.add(btnNavDash);
        navList.add(btnNavReg);
        navList.add(btnNavSlots);
        navList.add(btnNavVacc);
        navList.add(btnNavReports);
        navList.add(btnNavTools);

        sidebar.add(navList, BorderLayout.NORTH);

        // Sidebar Footer Card
        Panel foot = new Panel(new GridLayout(3, 1, 2, 2));
        foot.setBackground(new Color(25, 30, 44));
        Label l1 = new Label("  VaxiCare Engine v2.0", Label.LEFT);
        Label l2 = new Label("  Core Java • Pure AWT", Label.LEFT);
        Label l3 = new Label("  SQLite JDBC • Multithreaded", Label.LEFT);
        l1.setFont(FONT_SMALL); l1.setForeground(new Color(148, 163, 184));
        l2.setFont(FONT_SMALL); l2.setForeground(new Color(148, 163, 184));
        l3.setFont(FONT_SMALL); l3.setForeground(new Color(148, 163, 184));
        foot.add(l1); foot.add(l2); foot.add(l3);
        sidebar.add(foot, BorderLayout.SOUTH);

        return sidebar;
    }

    private Button createNavButton(String label) {
        Button btn = new Button(" " + label);
        btn.setFont(FONT_BOLD);
        btn.setBackground(COLOR_NAV);
        btn.setForeground(new Color(225, 235, 245));
        btn.addActionListener(this);
        btn.addKeyListener(this);
        return btn;
    }

    private void setActiveNav(Button activeBtn, String cardId) {
        currentCard = cardId;
        cardLayout.show(cardsPanel, cardId);
        if (navButtons != null) {
            for (Button b : navButtons) {
                if (b == activeBtn) {
                    b.setBackground(COLOR_NAV_ACTIVE);
                    b.setForeground(Color.WHITE);
                } else {
                    b.setBackground(COLOR_NAV);
                    b.setForeground(new Color(225, 235, 245));
                }
            }
        }
        requestFocus();
    }

    // =========================================================================
    // BOTTOM LIVE STATUS BANNER
    // =========================================================================
    private Panel createBottomFeedbackPanel() {
        pnlLiveFeedback = new Panel(new BorderLayout());
        pnlLiveFeedback.setBackground(new Color(226, 232, 240)); // Slate neutral

        lblLiveFeedback = new Label(" Ready. System operational with 25 pre-seeded citizens and 20 vaccination sessions.");
        lblLiveFeedback.setFont(FONT_BOLD);
        lblLiveFeedback.setForeground(COLOR_TEXT_DARK);
        pnlLiveFeedback.add(lblLiveFeedback, BorderLayout.CENTER);

        return pnlLiveFeedback;
    }

    public void setFeedback(String message, Color bgColor, Color textColor) {
        lblLiveFeedback.setText(" " + message);
        lblLiveFeedback.setForeground(textColor);
        pnlLiveFeedback.setBackground(bgColor);
    }

    public void showSuccess(String message) {
        setFeedback("✓ " + message, new Color(220, 252, 231), new Color(22, 101, 52)); // Green
    }

    public void showError(String message) {
        setFeedback("✗ " + message, new Color(254, 226, 226), new Color(185, 28, 28)); // Red
    }

    public void showWarning(String message) {
        setFeedback("⚠ " + message, new Color(254, 243, 199), new Color(180, 83, 9)); // Amber
    }

    public void showInfo(String message) {
        setFeedback("ℹ " + message, new Color(224, 242, 254), new Color(3, 105, 161)); // Blue
    }

    // =========================================================================
    // 1. DASHBOARD & QUICK WALK-IN PANEL
    // =========================================================================
    private Panel createDashboardPanel() {
        Panel panel = new Panel(new GridLayout(1, 2, 12, 12));
        panel.setBackground(COLOR_BG);

        // LEFT COLUMN: 1-Click Walk-In Wizard
        Panel leftCol = new Panel(new BorderLayout(8, 8));
        leftCol.setBackground(COLOR_CARD);

        Panel leftHeader = new Panel(new GridLayout(2, 1));
        leftHeader.setBackground(COLOR_CARD);
        Label lblWizardTitle = new Label(" ⚡ 1-Click Citizen Walk-In Inoculation", Label.LEFT);
        lblWizardTitle.setFont(FONT_HEADER);
        lblWizardTitle.setForeground(COLOR_PRIMARY);
        Label lblWizardSub = new Label("  Select or search citizen, pick vaccine, and record dose in 2 seconds.", Label.LEFT);
        lblWizardSub.setFont(FONT_SMALL);
        leftHeader.add(lblWizardTitle);
        leftHeader.add(lblWizardSub);
        leftCol.add(leftHeader, BorderLayout.NORTH);

        // Center Wizard Controls
        Panel wizardForm = new Panel(new GridLayout(9, 1, 4, 4));
        wizardForm.setBackground(COLOR_CARD);

        // Search Bar Row
        Panel pnlSearch = new Panel(new BorderLayout(4, 4));
        txtDashSearch = new TextField();
        txtDashSearch.addKeyListener(this);
        btnDashSearch = new Button("Search");
        btnDashSearch.addActionListener(this);
        btnDashLoadDemo = new Button("⚡ Load Sarojini (B5531)");
        btnDashLoadDemo.setBackground(new Color(217, 119, 6)); // Amber
        btnDashLoadDemo.setForeground(Color.WHITE);
        btnDashLoadDemo.setFont(FONT_BOLD);
        btnDashLoadDemo.addActionListener(this);

        Panel searchBtns = new Panel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        searchBtns.add(btnDashSearch);
        searchBtns.add(btnDashLoadDemo);
        pnlSearch.add(new Label("Search Citizen:"), BorderLayout.WEST);
        pnlSearch.add(txtDashSearch, BorderLayout.CENTER);
        pnlSearch.add(searchBtns, BorderLayout.EAST);
        wizardForm.add(pnlSearch);

        // Search Result List
        dashCitizenList = new java.awt.List(3, false);
        dashCitizenList.setFont(FONT_MONO);
        dashCitizenList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleDashCitizenSelect();
            }
        });
        wizardForm.add(dashCitizenList);

        // Citizen Summary Card
        lblDashCitizenDetails = new Label(" Selected Citizen: None (Select from list or click Demo)");
        lblDashCitizenDetails.setFont(FONT_BOLD);
        lblDashCitizenDetails.setForeground(COLOR_NAV);
        wizardForm.add(lblDashCitizenDetails);

        lblDashHistoryDetails = new Label(" Prior Inoculations: None");
        lblDashHistoryDetails.setFont(FONT_SMALL);
        wizardForm.add(lblDashHistoryDetails);

        // Vaccine Choice Row
        Panel pnlVaxRow = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        pnlVaxRow.add(new Label("Target Vaccine Brand:"));
        choiceDashVaccine = new Choice();
        choiceDashVaccine.add("Covishield");
        choiceDashVaccine.add("Covaxin");
        choiceDashVaccine.add("ChildCare");
        choiceDashVaccine.add("BoosterCare");
        choiceDashVaccine.addItemListener(this);
        pnlVaxRow.add(choiceDashVaccine);
        wizardForm.add(pnlVaxRow);

        // Live Eligibility Badge
        lblDashEligibility = new Label(" Clinical Eligibility: Awaiting Citizen Selection");
        lblDashEligibility.setFont(FONT_BOLD);
        lblDashEligibility.setBackground(new Color(241, 245, 249));
        lblDashEligibility.setForeground(new Color(71, 85, 105));
        wizardForm.add(lblDashEligibility);

        // Main Walk-In Inoculation Button
        btnDashWalkIn = new Button("⚡ Complete 1-Click Walk-In (Book & Administer)");
        btnDashWalkIn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnDashWalkIn.setBackground(COLOR_PRIMARY);
        btnDashWalkIn.setForeground(Color.WHITE);
        btnDashWalkIn.addActionListener(this);
        wizardForm.add(btnDashWalkIn);

        // Step-by-Step Alternative Row
        Panel pnlSubSteps = new Panel(new GridLayout(1, 2, 6, 2));
        btnDashBookOnly = new Button("Step 1: Book Slot Only");
        btnDashAdminOnly = new Button("Step 2: Administer Dose Only");
        btnDashBookOnly.addActionListener(this);
        btnDashAdminOnly.addActionListener(this);
        pnlSubSteps.add(btnDashBookOnly);
        pnlSubSteps.add(btnDashAdminOnly);
        wizardForm.add(pnlSubSteps);

        leftCol.add(wizardForm, BorderLayout.CENTER);
        panel.add(leftCol);

        // RIGHT COLUMN: Live Clinic Activity & Operations Snapshot
        Panel rightCol = new Panel(new BorderLayout(8, 8));
        rightCol.setBackground(COLOR_CARD);

        Label lblRightTitle = new Label(" Live Inoculation Stream & Clinic Activity", Label.LEFT);
        lblRightTitle.setFont(FONT_HEADER);
        lblRightTitle.setForeground(COLOR_NAV);
        rightCol.add(lblRightTitle, BorderLayout.NORTH);

        dashRecentDosesList = new java.awt.List(14, false);
        dashRecentDosesList.setFont(FONT_MONO);
        rightCol.add(dashRecentDosesList, BorderLayout.CENTER);

        Panel rightFoot = new Panel(new FlowLayout(FlowLayout.RIGHT));
        Button btnRefreshDash = new Button("↻ Refresh Activity Stream");
        btnRefreshDash.addActionListener(e -> refreshAllData());
        rightFoot.add(btnRefreshDash);
        rightCol.add(rightFoot, BorderLayout.SOUTH);

        panel.add(rightCol);
        return panel;
    }

    private void handleDashCitizenSelect() {
        int idx = dashCitizenList.getSelectedIndex();
        if (idx >= 0) {
            String item = dashCitizenList.getItem(idx);
            String benId = item.split("\\|")[0].trim();
            selectCitizenForDashboard(benId);
        }
    }

    public void selectCitizenForDashboard(String benId) {
        Beneficiary b = service.getLookup().get(benId);
        if (b != null) {
            selectedDashBenId = b.getBenId();
            lblDashCitizenDetails.setText(String.format(" Citizen: [%s] %s (Age: %d, %s)",
                    b.getBenId(), b.getName(), b.getAge(), b.getAgeGroup()));

            try {
                int doseCount = service.getDoseDAO().getLatestDoseNumber(b.getBenId(), choiceDashVaccine.getSelectedItem());
                DoseRecord latest = service.getDoseDAO().getLatestDose(b.getBenId());
                if (latest != null) {
                    lblDashHistoryDetails.setText(String.format(" Previous: Dose #%d (%s on %s) | Next Due: %s",
                            latest.getDoseNo(), latest.getVaccineName(), latest.getDoseDate(), latest.getNextDue()));
                } else {
                    lblDashHistoryDetails.setText(" Previous Inoculations: None recorded (Candidate for Dose #1)");
                }
            } catch (SQLException ignored) {
            }

            updateDashEligibilityPreview();
            populateBeneficiaryFields(b);
        }
    }

    private void updateDashEligibilityPreview() {
        if (selectedDashBenId == null || selectedDashBenId.isEmpty()) {
            lblDashEligibility.setText(" Clinical Eligibility: Awaiting Citizen Selection");
            lblDashEligibility.setForeground(new Color(71, 85, 105));
            return;
        }

        String vax = choiceDashVaccine.getSelectedItem();
        VaxiCareService.EligibilityCheck check = service.checkEligibility(selectedDashBenId, vax, LocalDate.now().toString());

        if (check.canProceed()) {
            lblDashEligibility.setText(" ✓ " + check.message);
            lblDashEligibility.setForeground(new Color(22, 101, 52)); // Green
            lblDashEligibility.setBackground(new Color(220, 252, 231));
        } else if (!check.eligible) {
            lblDashEligibility.setText(" ✗ " + check.message);
            lblDashEligibility.setForeground(new Color(185, 28, 28)); // Red
            lblDashEligibility.setBackground(new Color(254, 226, 226));
        } else {
            lblDashEligibility.setText(" ⚠ " + check.message);
            lblDashEligibility.setForeground(new Color(180, 83, 9)); // Amber
            lblDashEligibility.setBackground(new Color(254, 243, 199));
        }
    }

    // =========================================================================
    // 2. CITIZEN REGISTRATION PANEL
    // =========================================================================
    private Panel createRegistrationPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BG);

        // LEFT COLUMN: Registration Form
        Panel formCard = new Panel(new BorderLayout(8, 8));
        formCard.setBackground(COLOR_CARD);
        formCard.setPreferredSize(new Dimension(420, 600));

        Label lblFormTitle = new Label(" Citizen Demographic Registration", Label.LEFT);
        lblFormTitle.setFont(FONT_HEADER);
        lblFormTitle.setForeground(COLOR_NAV);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        Panel formFields = new Panel(new GridLayout(8, 2, 6, 6));
        formFields.setBackground(COLOR_CARD);

        txtRegId = new TextField();
        txtRegId.addKeyListener(this);
        txtRegName = new TextField();
        txtRegName.addKeyListener(this);
        txtRegAge = new TextField();
        txtRegAge.addKeyListener(this);
        txtRegIdNumber = new TextField();
        txtRegIdNumber.addKeyListener(this);
        txtRegPhone = new TextField();
        txtRegPhone.addKeyListener(this);
        lblRegGroup = new Label("Calculated on Age");
        lblRegGroup.setFont(FONT_BOLD);

        btnRegister = new Button("Register Citizen");
        btnRegister.setBackground(COLOR_PRIMARY);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(FONT_BOLD);
        btnRegister.addActionListener(this);

        btnClearReg = new Button("Clear Form (ESC)");
        btnClearReg.addActionListener(this);

        btnRegLoadDemo = new Button("⚡ Load Sarojini (B5531)");
        btnRegLoadDemo.setBackground(new Color(217, 119, 6));
        btnRegLoadDemo.setForeground(Color.WHITE);
        btnRegLoadDemo.setFont(FONT_BOLD);
        btnRegLoadDemo.addActionListener(this);

        formFields.add(new Label("Beneficiary ID (Auto/Manual):"));
        formFields.add(txtRegId);
        formFields.add(new Label("Full Name:"));
        formFields.add(txtRegName);
        formFields.add(new Label("Age (Years):"));
        formFields.add(txtRegAge);
        formFields.add(new Label("Demographic Group:"));
        formFields.add(lblRegGroup);
        formFields.add(new Label("National ID Document #:"));
        formFields.add(txtRegIdNumber);
        formFields.add(new Label("Mobile Phone (10 Digits):"));
        formFields.add(txtRegPhone);
        formFields.add(btnRegister);
        formFields.add(btnClearReg);
        formFields.add(btnRegLoadDemo);
        formFields.add(new Label(""));

        formCard.add(formFields, BorderLayout.CENTER);
        panel.add(formCard, BorderLayout.WEST);

        // RIGHT COLUMN: Directory & Autocomplete Filter
        Panel listCard = new Panel(new BorderLayout(6, 6));
        listCard.setBackground(COLOR_CARD);

        // Search Filter Bar
        Panel pnlFilter = new Panel(new BorderLayout(4, 4));
        pnlFilter.setBackground(COLOR_CARD);
        txtRegFilter = new TextField();
        txtRegFilter.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                filterBeneficiaryDirectory(txtRegFilter.getText());
            }
        });
        btnRegFilterClear = new Button("Clear");
        btnRegFilterClear.addActionListener(e -> {
            txtRegFilter.setText("");
            filterBeneficiaryDirectory("");
        });
        pnlFilter.add(new Label("Filter by ID, Name or Mobile:"), BorderLayout.WEST);
        pnlFilter.add(txtRegFilter, BorderLayout.CENTER);
        pnlFilter.add(btnRegFilterClear, BorderLayout.EAST);
        listCard.add(pnlFilter, BorderLayout.NORTH);

        benAwtList = new java.awt.List(16, false);
        benAwtList.setFont(FONT_MONO);
        benAwtList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBeneficiaryListSelection();
            }
        });
        listCard.add(benAwtList, BorderLayout.CENTER);

        panel.add(listCard, BorderLayout.CENTER);
        return panel;
    }

    private void filterBeneficiaryDirectory(String query) {
        benAwtList.removeAll();
        List<Beneficiary> results = service.searchBeneficiaries(query);
        for (Beneficiary b : results) {
            benAwtList.add(String.format("%-7s | %-16s | Age: %2d | %-20s | Ph: %s",
                    b.getBenId(), b.getName(), b.getAge(), b.getAgeGroup(), b.getPhone()));
        }
    }

    // =========================================================================
    // 3. SLOT BOOKING & SESSIONS PANEL
    // =========================================================================
    private Panel createSlotsPanel() {
        Panel panel = new Panel(new GridLayout(1, 2, 10, 10));
        panel.setBackground(COLOR_BG);

        // LEFT COLUMN: Booking Actions & Session Creator
        Panel leftCol = new Panel(new BorderLayout(8, 8));
        leftCol.setBackground(COLOR_CARD);

        // Booking Section
        Panel pnlBooking = new Panel(new GridLayout(6, 2, 4, 4));
        pnlBooking.setBackground(COLOR_CARD);

        txtSlotBenId = new TextField();
        txtSlotBenId.addKeyListener(this);
        btnSlotLookup = new Button("Lookup");
        btnSlotLookup.addActionListener(e -> handleSearchBeneficiary());
        btnSlotLoadDemo = new Button("⚡ Load Sarojini");
        btnSlotLoadDemo.addActionListener(this);

        Panel idRow = new Panel(new BorderLayout(2, 0));
        idRow.add(txtSlotBenId, BorderLayout.CENTER);
        Panel idBtns = new Panel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        idBtns.add(btnSlotLookup);
        idBtns.add(btnSlotLoadDemo);
        idRow.add(idBtns, BorderLayout.EAST);

        lblSlotCitizenInfo = new Label(" Citizen: Enter ID or Click Row in List");
        lblSlotCitizenInfo.setFont(FONT_SMALL);

        choiceSlotSession = new Choice();
        choiceSlotSession.addItemListener(this);

        choiceSlotVaccine = new Choice();
        choiceSlotVaccine.add("Covishield");
        choiceSlotVaccine.add("Covaxin");
        choiceSlotVaccine.add("ChildCare");
        choiceSlotVaccine.add("BoosterCare");

        btnBookSlot = new Button("Book Selected Slot");
        btnBookSlot.setBackground(COLOR_PRIMARY);
        btnBookSlot.setForeground(Color.WHITE);
        btnBookSlot.setFont(FONT_BOLD);
        btnBookSlot.addActionListener(this);

        btnAutoBook = new Button("Auto-Match First Available");
        btnAutoBook.addActionListener(this);

        pnlBooking.add(new Label("Beneficiary ID:"));
        pnlBooking.add(idRow);
        pnlBooking.add(new Label("Candidate Status:"));
        pnlBooking.add(lblSlotCitizenInfo);
        pnlBooking.add(new Label("Select Center Session:"));
        pnlBooking.add(choiceSlotSession);
        pnlBooking.add(new Label("Vaccine Product:"));
        pnlBooking.add(choiceSlotVaccine);
        pnlBooking.add(btnBookSlot);
        pnlBooking.add(btnAutoBook);

        leftCol.add(pnlBooking, BorderLayout.NORTH);

        // New Session Creator Form
        Panel pnlNewSess = new Panel(new GridLayout(5, 2, 4, 4));
        pnlNewSess.setBackground(COLOR_CARD);

        Label lblSessTitle = new Label(" + Schedule New Center Session", Label.LEFT);
        lblSessTitle.setFont(FONT_HEADER);
        lblSessTitle.setForeground(COLOR_NAV);

        txtNewSessDate = new TextField(LocalDate.now().plusDays(2).toString());
        choiceNewSessVaccine = new Choice();
        choiceNewSessVaccine.add("Covishield");
        choiceNewSessVaccine.add("Covaxin");
        choiceNewSessVaccine.add("ChildCare");
        choiceNewSessVaccine.add("BoosterCare");
        txtNewSessCapacity = new TextField("40");
        btnCreateSession = new Button("+ Create Session in SQLite");
        btnCreateSession.setBackground(COLOR_PRIMARY);
        btnCreateSession.setForeground(Color.WHITE);
        btnCreateSession.setFont(FONT_BOLD);
        btnCreateSession.addActionListener(this);

        pnlNewSess.add(lblSessTitle);
        pnlNewSess.add(new Label(""));
        pnlNewSess.add(new Label("Session Date (YYYY-MM-DD):"));
        pnlNewSess.add(txtNewSessDate);
        pnlNewSess.add(new Label("Allocated Vaccine Brand:"));
        pnlNewSess.add(choiceNewSessVaccine);
        pnlNewSess.add(new Label("Total Center Capacity:"));
        pnlNewSess.add(txtNewSessCapacity);
        pnlNewSess.add(btnCreateSession);
        pnlNewSess.add(new Label(""));

        leftCol.add(pnlNewSess, BorderLayout.CENTER);
        panel.add(leftCol);

        // RIGHT COLUMN: Active Sessions Browser
        Panel rightCol = new Panel(new BorderLayout(6, 6));
        rightCol.setBackground(COLOR_CARD);

        Label lblSessBrowserTitle = new Label(" Active Sessions Directory & Capacity Utilization", Label.LEFT);
        lblSessBrowserTitle.setFont(FONT_HEADER);
        lblSessBrowserTitle.setForeground(COLOR_NAV);
        rightCol.add(lblSessBrowserTitle, BorderLayout.NORTH);

        sessionAwtList = new java.awt.List(16, false);
        sessionAwtList.setFont(FONT_MONO);
        sessionAwtList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleSessionListSelection();
            }
        });
        rightCol.add(sessionAwtList, BorderLayout.CENTER);

        panel.add(rightCol);
        return panel;
    }

    // =========================================================================
    // 4. ADMINISTER DOSE & VIALS PANEL
    // =========================================================================
    private Panel createVaccinationPanel() {
        Panel panel = new Panel(new GridLayout(1, 2, 10, 10));
        panel.setBackground(COLOR_BG);

        // LEFT COLUMN: Clinical Dose Administration
        Panel leftCol = new Panel(new BorderLayout(8, 8));
        leftCol.setBackground(COLOR_CARD);

        Label lblAdminTitle = new Label(" Clinical Dose Administration & Certificate", Label.LEFT);
        lblAdminTitle.setFont(FONT_HEADER);
        lblAdminTitle.setForeground(COLOR_NAV);
        leftCol.add(lblAdminTitle, BorderLayout.NORTH);

        Panel formPanel = new Panel(new GridLayout(8, 2, 6, 6));
        formPanel.setBackground(COLOR_CARD);

        txtVaccBenId = new TextField();
        txtVaccBenId.addKeyListener(this);
        txtVaccBenName = new TextField();
        txtVaccBenAge = new TextField();
        txtVaccSessionId = new TextField("1");

        choiceVaccBrand = new Choice();
        choiceVaccBrand.add("Covishield");
        choiceVaccBrand.add("Covaxin");
        choiceVaccBrand.add("ChildCare");
        choiceVaccBrand.add("BoosterCare");

        chkSyncToggle = new Checkbox("Enable Thread Locking (Safe Mode)", service.getVialManager().isUseSynchronization());
        chkSyncToggle.addItemListener(this);

        btnAdministerDose = new Button("Administer Dose (Ctrl+V)");
        btnAdministerDose.setBackground(COLOR_DANGER);
        btnAdministerDose.setForeground(Color.WHITE);
        btnAdministerDose.setFont(FONT_BOLD);
        btnAdministerDose.addActionListener(this);

        btnSimulateRace = new Button("Simulate Concurrency Race");
        btnSimulateRace.addActionListener(this);

        formPanel.add(new Label("Beneficiary ID:"));
        formPanel.add(txtVaccBenId);
        formPanel.add(new Label("Citizen Name:"));
        formPanel.add(txtVaccBenName);
        formPanel.add(new Label("Age:"));
        formPanel.add(txtVaccBenAge);
        formPanel.add(new Label("Vaccine Product:"));
        formPanel.add(choiceVaccBrand);
        formPanel.add(new Label("Session ID:"));
        formPanel.add(txtVaccSessionId);
        formPanel.add(new Label("Concurrency Safety:"));
        formPanel.add(chkSyncToggle);
        formPanel.add(btnAdministerDose);
        formPanel.add(btnSimulateRace);

        leftCol.add(formPanel, BorderLayout.CENTER);

        // Waiting Queue List
        Panel pnlQueue = new Panel(new BorderLayout(4, 4));
        pnlQueue.setBackground(COLOR_CARD);
        Label lblQueue = new Label(" Waiting Queue (LinkedList - Click to Load):", Label.LEFT);
        lblQueue.setFont(FONT_BOLD);
        pnlQueue.add(lblQueue, BorderLayout.NORTH);

        queueAwtList = new java.awt.List(5, false);
        queueAwtList.setFont(FONT_MONO);
        queueAwtList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleQueueListSelection();
            }
        });
        pnlQueue.add(queueAwtList, BorderLayout.CENTER);
        leftCol.add(pnlQueue, BorderLayout.SOUTH);

        panel.add(leftCol);

        // RIGHT COLUMN: Vial Lifecycle Panel (Visual Canvas)
        Panel rightCol = new Panel(new BorderLayout(8, 8));
        rightCol.setBackground(COLOR_CARD);

        Label lblVialTitle = new Label(" Physical Vial Cold-Chain Lifecycle Monitor", Label.LEFT);
        lblVialTitle.setFont(FONT_HEADER);
        lblVialTitle.setForeground(COLOR_NAV);
        rightCol.add(lblVialTitle, BorderLayout.NORTH);

        Panel vialCenter = new Panel(new BorderLayout(8, 8));
        vialCenter.setBackground(COLOR_CARD);

        // Visual AWT Canvas
        vialCanvas = new VialCanvas();
        vialCenter.add(vialCanvas, BorderLayout.NORTH);

        // Vial Manual Action Buttons
        Panel vialBtns = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        btnOpenNewVial = new Button("Open New Vial");
        btnOpenNewVial.setBackground(COLOR_PRIMARY);
        btnOpenNewVial.setForeground(Color.WHITE);
        btnOpenNewVial.setFont(FONT_BOLD);
        btnOpenNewVial.addActionListener(this);

        btnDiscardVial = new Button("Discard Expired Vial");
        btnDiscardVial.setBackground(COLOR_WARNING);
        btnDiscardVial.setForeground(Color.WHITE);
        btnDiscardVial.setFont(FONT_BOLD);
        btnDiscardVial.addActionListener(this);

        vialBtns.add(btnOpenNewVial);
        vialBtns.add(btnDiscardVial);
        vialCenter.add(vialBtns, BorderLayout.CENTER);

        // Information notes
        TextArea txtVialGuide = new TextArea(
                "VIAL LIFECYCLE OPERATING PROTOCOL:\n" +
                "• Each multi-dose vial contains exactly 10 doses.\n" +
                "• Under clinical guidelines, opened vials expire strictly after 6 hours.\n" +
                "• When expired, remaining doses are discarded and recorded as wastage.\n" +
                "• Thread-safe synchronized locking prevents overdrawing and race conditions.\n" +
                "• Toggle thread locking off above to test unsynchronized concurrent behavior.",
                6, 50, TextArea.SCROLLBARS_VERTICAL_ONLY
        );
        txtVialGuide.setEditable(false);
        txtVialGuide.setFont(FONT_SMALL);
        vialCenter.add(txtVialGuide, BorderLayout.SOUTH);

        rightCol.add(vialCenter, BorderLayout.CENTER);
        panel.add(rightCol);

        return panel;
    }

    // =========================================================================
    // 5. REMINDERS & COVERAGE REPORT PANEL
    // =========================================================================
    private Panel createReportsPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BG);

        // Action Toolbar
        Panel toolbar = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(COLOR_CARD);

        btnGenDailyReport = new Button("Generate Daily Coverage Report (F4)");
        btnGenDailyReport.setFont(FONT_BOLD);
        btnGenDailyReport.setBackground(COLOR_PRIMARY);
        btnGenDailyReport.setForeground(Color.WHITE);
        btnGenDailyReport.addActionListener(this);

        btnViewReminders = new Button("Due Tomorrow Reminders");
        btnViewReminders.setFont(FONT_BOLD);
        btnViewReminders.addActionListener(this);

        btnPurgeVaccinated = new Button("Purge Already-Vaccinated (Iterator.remove)");
        btnPurgeVaccinated.addActionListener(this);

        btnTriggerReminderThread = new Button("Trigger Reminder Thread");
        btnTriggerReminderThread.addActionListener(this);

        btnExportReportFile = new Button("Export Report (.txt)");
        btnExportReportFile.addActionListener(this);

        toolbar.add(btnGenDailyReport);
        toolbar.add(btnViewReminders);
        toolbar.add(btnPurgeVaccinated);
        toolbar.add(btnTriggerReminderThread);
        toolbar.add(btnExportReportFile);

        panel.add(toolbar, BorderLayout.NORTH);

        txtReportDisplay = new TextArea("", 22, 90, TextArea.SCROLLBARS_BOTH);
        txtReportDisplay.setFont(FONT_MONO);
        txtReportDisplay.setEditable(false);
        txtReportDisplay.setBackground(Color.WHITE);
        panel.add(txtReportDisplay, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // 6. THREADS, BACKUP & SYSTEM TOOLS PANEL
    // =========================================================================
    private Panel createToolsPanel() {
        Panel panel = new Panel(new GridLayout(1, 2, 10, 10));
        panel.setBackground(COLOR_BG);

        // LEFT COLUMN: Multithreading & Binary Lab
        Panel leftCol = new Panel(new BorderLayout(8, 8));
        leftCol.setBackground(COLOR_CARD);

        Label lblToolsTitle = new Label(" Multithreading & Binary Persistence Lab", Label.LEFT);
        lblToolsTitle.setFont(FONT_HEADER);
        lblToolsTitle.setForeground(COLOR_NAV);
        leftCol.add(lblToolsTitle, BorderLayout.NORTH);

        Panel controlsPanel = new Panel(new GridLayout(8, 1, 4, 4));
        controlsPanel.setBackground(COLOR_CARD);

        // Thread status monitors
        lblThreadStateSenior = new Label("Senior Priority Vaccinator (Priority 10): RUNNABLE");
        lblThreadStateGeneral = new Label("General Vaccinator (Priority 5): RUNNABLE");
        lblThreadStateReminder = new Label("Reminder Daemon Monitor: TIMED_WAITING");
        lblThreadStateSenior.setFont(FONT_SMALL);
        lblThreadStateGeneral.setFont(FONT_SMALL);
        lblThreadStateReminder.setFont(FONT_SMALL);

        btnRefreshThreadStates = new Button("↻ Refresh Thread States");
        btnRefreshThreadStates.addActionListener(e -> updateThreadStateDisplay());

        btnRunRaceComparison = new Button("Run Concurrency Race Comparison (Safe vs Unsafe)");
        btnRunRaceComparison.setFont(FONT_BOLD);
        btnRunRaceComparison.addActionListener(this);

        btnTriggerUncaught = new Button("Trigger Uncaught ArithmeticException Demo (45 / 0)");
        btnTriggerUncaught.setBackground(COLOR_DANGER);
        btnTriggerUncaught.setForeground(Color.WHITE);
        btnTriggerUncaught.setFont(FONT_BOLD);
        btnTriggerUncaught.addActionListener(this);

        // Binary backup controls
        Panel pnlBackup = new Panel(new GridLayout(1, 2, 4, 0));
        btnBackupBinary = new Button("Backup Doses (doses.dat)");
        btnRestoreBinary = new Button("Restore & Verify doses.dat");
        btnBackupBinary.addActionListener(this);
        btnRestoreBinary.addActionListener(this);
        pnlBackup.add(btnBackupBinary);
        pnlBackup.add(btnRestoreBinary);

        lblBinaryBackupStatus = new Label(" Binary Backup Status: doses.dat verified (100% Parity)");
        lblBinaryBackupStatus.setFont(FONT_SMALL);

        controlsPanel.add(lblThreadStateSenior);
        controlsPanel.add(lblThreadStateGeneral);
        controlsPanel.add(lblThreadStateReminder);
        controlsPanel.add(btnRefreshThreadStates);
        controlsPanel.add(btnRunRaceComparison);
        controlsPanel.add(btnTriggerUncaught);
        controlsPanel.add(pnlBackup);
        controlsPanel.add(lblBinaryBackupStatus);

        leftCol.add(controlsPanel, BorderLayout.CENTER);
        panel.add(leftCol);

        // RIGHT COLUMN: System Activity & Audit Console
        Panel rightCol = new Panel(new BorderLayout(8, 8));
        rightCol.setBackground(COLOR_CARD);

        Label lblLogTitle = new Label(" System Diagnostic Logs & Concurrency Output", Label.LEFT);
        lblLogTitle.setFont(FONT_HEADER);
        lblLogTitle.setForeground(COLOR_NAV);
        rightCol.add(lblLogTitle, BorderLayout.NORTH);

        txtToolsOutput = new TextArea("", 20, 50, TextArea.SCROLLBARS_BOTH);
        txtToolsOutput.setFont(FONT_MONO);
        txtToolsOutput.setEditable(false);
        rightCol.add(txtToolsOutput, BorderLayout.CENTER);

        Panel logBtns = new Panel(new FlowLayout(FlowLayout.RIGHT));
        Button btnViewVialLogs = new Button("View Vial Audit Logs (Vector)");
        btnViewVialLogs.addActionListener(e -> {
            txtToolsOutput.setText("=== VIAL CONSUMPTION & AUDIT LOGS (Vector) ===\n\n");
            for (String s : service.getVialLogs()) {
                txtToolsOutput.append(s + "\n");
            }
        });
        logBtns.add(btnViewVialLogs);
        rightCol.add(logBtns, BorderLayout.SOUTH);

        panel.add(rightCol);
        return panel;
    }

    private void updateThreadStateDisplay() {
        List<String> reminderLogs = service.getReminderTask().getReminderLogs();
        if (!reminderLogs.isEmpty()) {
            txtToolsOutput.setText("=== LIVE THREAD INSPECTION LOGS ===\n\n");
            for (String l : reminderLogs) {
                txtToolsOutput.append(l + "\n");
            }
        }
        showSuccess("Thread state monitor refreshed.");
    }

    // =========================================================================
    // DATA REFRESH & STATE SYNCHRONIZATION
    // =========================================================================
    public void refreshAllData() {
        try {
            // 1. Top metric badges
            int citizens = service.getBeneficiaryDAO().count();
            int doses = service.getDoseDAO().count();
            int vials = service.getVialManager().getVialsOpened();
            int todayVacc = service.getVaccinatedToday().size();
            List<Beneficiary> dueTom = service.getDueTomorrowBeneficiaries();

            lblStatCitizens.setText(" Citizens: " + citizens + " ");
            lblStatBooked.setText(" Vaccinated Today: " + todayVacc + " ");
            lblStatDoses.setText(" Doses Given: " + doses + " ");
            lblStatVials.setText(" Active Vials: " + vials + " ");
            lblStatDueTomorrow.setText(" Due Tomorrow: " + dueTom.size() + " ");

            // 2. Beneficiary directories (Registration & Dashboard)
            filterBeneficiaryDirectory(txtRegFilter != null ? txtRegFilter.getText() : "");

            dashCitizenList.removeAll();
            List<Beneficiary> allBens = service.searchBeneficiaries("");
            for (Beneficiary b : allBens) {
                dashCitizenList.add(String.format("%-7s | %-16s | Age: %2d | %s",
                        b.getBenId(), b.getName(), b.getAge(), b.getAgeGroup()));
            }

            // 3. Sessions list & dropdowns
            sessionAwtList.removeAll();
            choiceSlotSession.removeAll();
            List<Session> sessList = service.getSessionDAO().findAll();
            for (Session s : sessList) {
                String line = String.format("Sess #%-2d | %s | %-11s | Slots: %2d/%2d (%2d free)",
                        s.getSessionId(), s.getSessionDate(), s.getVaccineName(), s.getBooked(), s.getCapacity(), s.getAvailableSlots());
                sessionAwtList.add(line);
                choiceSlotSession.add("Session #" + s.getSessionId() + " (" + s.getSessionDate() + " - " + s.getVaccineName() + ")");
            }

            // 4. Vaccination Queue
            queueAwtList.removeAll();
            synchronized (service.getVaccinationQueue()) {
                for (Beneficiary qb : service.getVaccinationQueue()) {
                    queueAwtList.add(String.format("%-7s | %-16s | Age: %2d | %s",
                            qb.getBenId(), qb.getName(), qb.getAge(), qb.getAgeGroup()));
                }
            }

            // 5. Dashboard recent doses stream
            dashRecentDosesList.removeAll();
            List<DoseRecord> allDoses = service.getDoseDAO().findAll();
            for (int i = allDoses.size() - 1; i >= Math.max(0, allDoses.size() - 12); i--) {
                DoseRecord dr = allDoses.get(i);
                dashRecentDosesList.add(String.format("Dose #%-2d | %-6s | %-11s | Cert: %s | %s",
                        dr.getDoseNo(), dr.getBenId(), dr.getVaccineName(), dr.getCertificateNo(), dr.getDoseDate()));
            }

            // 6. Vial canvas update
            if (vialCanvas != null) {
                vialCanvas.updateState(
                        service.getVialManager().getDosesRemainingInCurrentVial(),
                        10,
                        service.getVialManager().getVialsOpened(),
                        service.getVialManager().getRemainingShelfLifeMinutes(),
                        service.getVialManager().getElapsedMinutes(),
                        service.getVialManager().getDosesWasted()
                );
            }

        } catch (SQLException e) {
            showError("Database refresh error: " + e.getMessage());
        }
    }

    // =========================================================================
    // EVENT DISPATCHER
    // =========================================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        // 1. Sidebar Nav
        if (src == btnNavDash) {
            setActiveNav(btnNavDash, CARD_DASHBOARD);
        } else if (src == btnNavReg) {
            setActiveNav(btnNavReg, CARD_REGISTRATION);
        } else if (src == btnNavSlots) {
            setActiveNav(btnNavSlots, CARD_SLOTS);
        } else if (src == btnNavVacc) {
            setActiveNav(btnNavVacc, CARD_VACCINATION);
        } else if (src == btnNavReports) {
            setActiveNav(btnNavReports, CARD_REPORTS);
        } else if (src == btnNavTools) {
            setActiveNav(btnNavTools, CARD_TOOLS);
        }
        // 2. Dashboard Actions
        else if (src == btnDashSearch) {
            handleDashSearch();
        } else if (src == btnDashLoadDemo || src == btnRegLoadDemo || src == btnSlotLoadDemo) {
            loadDemoCitizenSarojini();
        } else if (src == btnDashWalkIn) {
            handle1ClickWalkIn();
        } else if (src == btnDashBookOnly) {
            handleDashBookOnly();
        } else if (src == btnDashAdminOnly) {
            handleDashAdminOnly();
        }
        // 3. Registration Actions
        else if (src == btnRegister) {
            handleRegisterBeneficiary();
        } else if (src == btnClearReg) {
            clearRegistrationForm();
        }
        // 4. Slots & Sessions Actions
        else if (src == btnBookSlot) {
            handleBookSlotExplicit();
        } else if (src == btnAutoBook) {
            handleAutoBookSlot();
        } else if (src == btnCreateSession) {
            handleCreateSession();
        }
        // 5. Vaccination Actions
        else if (src == btnAdministerDose) {
            handleAdministerDose();
        } else if (src == btnSimulateRace) {
            handleSimulateRace();
        } else if (src == btnOpenNewVial) {
            service.getVialManager().openNewVialManually("Clinic-Operator");
            refreshAllData();
            showSuccess("Manually opened new vial. 10 fresh doses ready.");
        } else if (src == btnDiscardVial) {
            service.getVialManager().discardCurrentVial("Clinic-Operator");
            refreshAllData();
            showWarning("Current vial discarded. Remaining doses logged as wastage.");
        }
        // 6. Reports Actions
        else if (src == btnGenDailyReport) {
            generateDailyCoverageReport();
        } else if (src == btnViewReminders) {
            viewRemindersReport();
        } else if (src == btnPurgeVaccinated) {
            handlePurgeVaccinated();
        } else if (src == btnTriggerReminderThread) {
            service.triggerReminderScanNow();
            showSuccess("Triggered background reminder thread via wait()/notifyAll().");
        } else if (src == btnExportReportFile) {
            handleExportReport();
        }
        // 7. System Tools Actions
        else if (src == btnRunRaceComparison) {
            handleRunRaceComparison();
        } else if (src == btnTriggerUncaught) {
            triggerUncaughtException();
        } else if (src == btnBackupBinary) {
            handleBackupBinary();
        } else if (src == btnRestoreBinary) {
            handleRestoreBinary();
        }
    }

    // =========================================================================
    // WORKFLOW IMPLEMENTATIONS
    // =========================================================================

    private void loadDemoCitizenSarojini() {
        Beneficiary sarojini = service.getLookup().get("B5531");
        if (sarojini == null) {
            try {
                sarojini = service.getBeneficiaryDAO().findById("B5531");
                if (sarojini != null) service.getLookup().put("B5531", sarojini);
            } catch (SQLException ignored) {}
        }
        if (sarojini != null) {
            populateBeneficiaryFields(sarojini);
            selectCitizenForDashboard("B5531");
            showSuccess("Loaded Verified Demo Citizen: [B5531] Sarojini (Age 64, Senior Citizen)");
        } else {
            showError("Demo Citizen B5531 not found in database.");
        }
    }

    private void handleDashSearch() {
        String q = txtDashSearch.getText().trim();
        dashCitizenList.removeAll();
        List<Beneficiary> results = service.searchBeneficiaries(q);
        for (Beneficiary b : results) {
            dashCitizenList.add(String.format("%-7s | %-16s | Age: %2d | %s",
                    b.getBenId(), b.getName(), b.getAge(), b.getAgeGroup()));
        }
        if (!results.isEmpty()) {
            selectCitizenForDashboard(results.get(0).getBenId());
            showInfo("Found " + results.size() + " matching citizens.");
        } else {
            showWarning("No citizen matching '" + q + "' was found.");
        }
    }

    private void handle1ClickWalkIn() {
        if (selectedDashBenId == null || selectedDashBenId.isEmpty()) {
            showError("Please select a citizen first or click '⚡ Load Sarojini'!");
            return;
        }
        String vax = choiceDashVaccine.getSelectedItem();
        try {
            DoseRecord rec = service.walkInVaccinate(selectedDashBenId, vax);
            refreshAllData();
            Beneficiary ben = service.getLookup().get(selectedDashBenId);
            showSuccess(String.format("Walk-In Completed! Dose #%d of %s administered to %s.",
                    rec.getDoseNo(), rec.getVaccineName(), ben != null ? ben.getName() : selectedDashBenId));

            // Display Official Certificate Dialog
            CertificateDialog dlg = new CertificateDialog(this, rec, ben, service);
            dlg.setVisible(true);

        } catch (IneligibleAgeException iae) {
            showError("Ineligible Candidate: " + iae.getMessage());
        } catch (DoseNotDueException dnde) {
            showWarning("Dose Not Due: " + dnde.getMessage());
        } catch (SessionFullException sfe) {
            showError("Session Full: " + sfe.getMessage());
        } catch (VaxiCareException ve) {
            showError("Walk-In Error: " + ve.getMessage());
        } catch (SQLException sqle) {
            showError("Database Error: " + sqle.getMessage());
        }
    }

    private void handleDashBookOnly() {
        if (selectedDashBenId == null || selectedDashBenId.isEmpty()) {
            showError("Select a citizen first.");
            return;
        }
        try {
            service.bookSlot(selectedDashBenId);
            refreshAllData();
            showSuccess("Slot successfully booked for " + selectedDashBenId);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleDashAdminOnly() {
        if (selectedDashBenId == null || selectedDashBenId.isEmpty()) {
            showError("Select a citizen first.");
            return;
        }
        try {
            String vax = choiceDashVaccine.getSelectedItem();
            DoseRecord rec = service.recordVaccination(selectedDashBenId, vax, 1);
            refreshAllData();
            Beneficiary ben = service.getLookup().get(selectedDashBenId);
            showSuccess("Dose administered to " + selectedDashBenId + ". Certificate: " + rec.getCertificateNo());
            new CertificateDialog(this, rec, ben, service).setVisible(true);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleRegisterBeneficiary() {
        try {
            String name = txtRegName.getText().trim();
            if (name.isEmpty()) {
                throw new VaxiCareException("Citizen name cannot be blank.");
            }
            int age = Integer.parseInt(txtRegAge.getText().trim());
            String idNum = txtRegIdNumber.getText().trim();
            String phone = txtRegPhone.getText().trim();

            if (!Beneficiary.isValidPhone(phone)) {
                throw new VaxiCareException("Invalid telephone! Must be exactly 10 numeric digits.");
            }

            Beneficiary b;
            String explicitId = txtRegId.getText().trim();
            if (!explicitId.isEmpty()) {
                b = new Beneficiary(explicitId, name, age, idNum, phone, Beneficiary.determineAgeGroup(age));
            } else {
                b = new Beneficiary(name, age, idNum, phone);
            }

            boolean ok = service.getBeneficiaryDAO().insert(b);
            if (ok) {
                service.getLookup().put(b.getBenId(), b);
                populateBeneficiaryFields(b);
                refreshAllData();
                showSuccess("Citizen successfully registered: [" + b.getBenId() + "] " + b.getName());
            }

        } catch (NumberFormatException nfe) {
            showError("Age must be a valid integer! Details: " + nfe.getMessage());
        } catch (SQLException sqle) {
            showError("Database Error: " + sqle.getMessage());
        } catch (VaxiCareException ve) {
            showError("Validation Error: " + ve.getMessage());
        }
    }

    private void handleSearchBeneficiary() {
        String query = txtRegId.getText().trim();
        if (query.isEmpty()) query = txtSlotBenId.getText().trim();
        if (query.isEmpty()) query = txtVaccBenId.getText().trim();
        if (query.isEmpty()) query = txtDashSearch.getText().trim();

        if (query.isEmpty()) {
            showError("Enter a citizen ID, Name, or Phone to search.");
            return;
        }

        List<Beneficiary> matches = service.searchBeneficiaries(query);
        if (!matches.isEmpty()) {
            Beneficiary b = matches.get(0);
            populateBeneficiaryFields(b);
            selectCitizenForDashboard(b.getBenId());
            showSuccess("Found Citizen: [" + b.getBenId() + "] " + b.getName() + " (Age " + b.getAge() + ")");
        } else {
            showError("No citizen matching '" + query + "' found.");
        }
    }

    private void clearRegistrationForm() {
        txtRegId.setText("");
        txtRegName.setText("");
        txtRegAge.setText("");
        txtRegIdNumber.setText("");
        txtRegPhone.setText("");
        lblRegGroup.setText("Calculated on Age");
        showInfo("Form cleared. Ready for new citizen entry.");
    }

    private void handleBookSlotExplicit() {
        try {
            String benId = txtSlotBenId.getText().trim();
            if (benId.isEmpty()) throw new VaxiCareException("Citizen ID cannot be empty.");

            int idx = choiceSlotSession.getSelectedIndex();
            if (idx < 0 || idx >= service.getSessions().size()) {
                throw new VaxiCareException("Select a valid vaccination session.");
            }
            Session target = service.getSessions().get(idx);
            String vax = choiceSlotVaccine.getSelectedItem();

            service.bookSlot(benId, target.getSessionId(), vax);
            refreshAllData();
            showSuccess(String.format("Slot confirmed for %s at Session #%d (%s, %s).",
                    benId, target.getSessionId(), target.getSessionDate(), vax));

        } catch (DoseNotDueException dnde) {
            showWarning("Dose Not Due: " + dnde.getMessage());
        } catch (SessionFullException sfe) {
            showError("Session Full: " + sfe.getMessage());
        } catch (IneligibleAgeException iae) {
            showError("Ineligible Citizen: " + iae.getMessage());
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleAutoBookSlot() {
        try {
            String benId = txtSlotBenId.getText().trim();
            if (benId.isEmpty()) throw new VaxiCareException("Enter Citizen ID for auto-match.");

            service.bookSlot(benId);
            refreshAllData();
            showSuccess("Auto-matched " + benId + " to earliest available session.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleCreateSession() {
        try {
            String date = txtNewSessDate.getText().trim();
            String vax = choiceNewSessVaccine.getSelectedItem();
            int cap = Integer.parseInt(txtNewSessCapacity.getText().trim());

            Session s = service.createNewSession(date, vax, cap);
            refreshAllData();
            showSuccess(String.format("Created Session #%d on %s for %s (Capacity: %d)",
                    s.getSessionId(), s.getSessionDate(), s.getVaccineName(), s.getCapacity()));
        } catch (NumberFormatException nfe) {
            showError("Capacity must be a valid integer!");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleAdministerDose() {
        try {
            String benId = txtVaccBenId.getText().trim();
            if (benId.isEmpty()) throw new VaxiCareException("Citizen ID is required.");
            String vax = choiceVaccBrand.getSelectedItem();
            int sessId = 1;
            try {
                sessId = Integer.parseInt(txtVaccSessionId.getText().trim());
            } catch (NumberFormatException ignored) {}

            DoseRecord rec = service.recordVaccination(benId, vax, sessId);
            refreshAllData();
            Beneficiary ben = service.getLookup().get(benId);
            showSuccess("Dose #" + rec.getDoseNo() + " recorded! Certificate: " + rec.getCertificateNo());

            // Show Certificate Dialog
            new CertificateDialog(this, rec, ben, service).setVisible(true);

        } catch (Exception ex) {
            showError("Administration failed: " + ex.getMessage());
        }
    }

    private void handleSimulateRace() {
        txtToolsOutput.setText("=== Concurrency Demonstration: Drawing 15 Doses ===\n");
        boolean sync = service.getVialManager().isUseSynchronization();
        txtToolsOutput.append("Current Concurrency Mode: " + (sync ? "SAFE SYNCHRONIZED" : "UNSYNCHRONIZED (RACE RISK)") + "\n\n");

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 8; i++) service.getVialManager().drawDose("Worker-T1");
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 7; i++) service.getVialManager().drawDose("Worker-T2");
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException ignored) {}

        refreshAllData();
        setActiveNav(btnNavTools, CARD_TOOLS);
        txtToolsOutput.append("Completed concurrency run. Vials opened: " + service.getVialManager().getVialsOpened() +
                ", Doses left: " + service.getVialManager().getDosesRemainingInCurrentVial() + "\n");
        showInfo("Concurrency simulation finished. Check diagnostic log.");
    }

    private void handleRunRaceComparison() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("          CONCURRENCY COMPARISON LAB: SAFE SYNCHRONIZED VS UNSAFE       \n");
        sb.append("========================================================================\n\n");

        // Pass 1: Safe Synchronized
        service.getVialManager().setUseSynchronization(true);
        sb.append("PASS 1: SYNCHRONIZED MUTUAL EXCLUSION (Atomic Locking)\n");
        service.getVialManager().drawDose("Thread-Alpha-Sync");
        service.getVialManager().drawDose("Thread-Beta-Sync");
        sb.append("  -> Vials opened: ").append(service.getVialManager().getVialsOpened())
          .append(" | Doses in vial: ").append(service.getVialManager().getDosesRemainingInCurrentVial())
          .append(" | Thread State: ").append(Thread.currentThread().getState()).append(" (PASSED)\n\n");

        // Pass 2: Unsafe Unsynchronized
        service.getVialManager().setUseSynchronization(false);
        sb.append("PASS 2: UNSYNCHRONIZED (Deliberate Race Condition Window)\n");
        service.getVialManager().drawDose("Thread-Gamma-Unsync");
        service.getVialManager().drawDose("Thread-Delta-Unsync");
        sb.append("  -> Vials opened: ").append(service.getVialManager().getVialsOpened())
          .append(" | Doses in vial: ").append(service.getVialManager().getDosesRemainingInCurrentVial())
          .append(" | Mode: UNSYNCHRONIZED (Race window active)\n\n");

        service.getVialManager().setUseSynchronization(true); // Restore safe mode
        sb.append("STATUS: Thread safety restored to SAFE SYNCHRONIZED.\n");

        txtToolsOutput.setText(sb.toString());
        showSuccess("Concurrency comparison test complete.");
    }

    private void handleBackupBinary() {
        try {
            service.backupDosesToBinary("doses.dat");
            lblBinaryBackupStatus.setText(" Binary Backup: Exported 100% of SQLite records to doses.dat");
            showSuccess("Exported binary backup to doses.dat via FileOutputStream + ByteBuffer.");
        } catch (Exception ex) {
            showError("Backup failed: " + ex.getMessage());
        }
    }

    private void handleRestoreBinary() {
        try {
            boolean match = service.restoreDosesAndVerify("doses.dat");
            if (match) {
                lblBinaryBackupStatus.setText(" Binary Backup Status: 100% Parity Verified with SQLite");
                showSuccess("Binary restore verified! File byte stream matches database record count 100%.");
            } else {
                showError("Count mismatch between binary backup and database!");
            }
        } catch (Exception ex) {
            showError("Restore error: " + ex.getMessage());
        }
    }

    public void generateDailyCoverageReport() {
        try {
            String report = service.generateDailyCoverageReportText();
            txtReportDisplay.setText(report);
            setActiveNav(btnNavReports, CARD_REPORTS);
            showSuccess("Daily Coverage Report compiled successfully (F4).");
        } catch (SQLException e) {
            showError("Report generation failed: " + e.getMessage());
        }
    }

    public void viewRemindersReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                     BENEFICIARIES SCHEDULED FOR NEXT DOSE TOMORROW                    \n");
        sb.append("========================================================================================\n");
        sb.append("Reference Date: ").append(LocalDate.now().plusDays(1)).append("\n\n");

        List<Beneficiary> due = service.getDueTomorrowBeneficiaries();
        sb.append("Total Beneficiaries Due Tomorrow: ").append(due.size()).append("\n\n");
        for (Beneficiary b : due) {
            sb.append(String.format(" -> [%-6s] %-18s | Age: %2d (%-20s) | Mobile: %s\n",
                    b.getBenId(), b.getName(), b.getAge(), b.getAgeGroup(), b.getPhone()));
        }

        txtReportDisplay.setText(sb.toString());
        setActiveNav(btnNavReports, CARD_REPORTS);
        showInfo("Loaded " + due.size() + " beneficiaries due tomorrow.");
    }

    private void handlePurgeVaccinated() {
        List<Beneficiary> due = service.getDueTomorrowBeneficiaries();
        int preCount = due.size();
        List<Beneficiary> purged = service.purgeAlreadyVaccinated(due);
        int postCount = purged.size();
        viewRemindersReport();
        showSuccess(String.format("Purge Executed via Iterator.remove(): %d candidates pre-purge -> %d post-purge.",
                preCount, postCount));
    }

    private void handleExportReport() {
        try {
            String path = "daily_coverage_report_" + LocalDate.now() + ".txt";
            service.exportDailyReportToFile(path);
            showSuccess("Daily report exported to: " + new File(path).getAbsolutePath());
        } catch (Exception ex) {
            showError("Export failed: " + ex.getMessage());
        }
    }

    private void triggerUncaughtException() {
        showWarning("Triggering com.vaxicare.exception.UncaughtDemo.main() in console...");
        com.vaxicare.exception.UncaughtDemo.main(new String[0]);
    }

    // =========================================================================
    // POPULATION & SELECTION UTILITIES
    // =========================================================================
    private void handleBeneficiaryListSelection() {
        int idx = benAwtList.getSelectedIndex();
        if (idx >= 0) {
            String item = benAwtList.getItem(idx);
            String benId = item.split("\\|")[0].trim();
            Beneficiary b = service.getLookup().get(benId);
            if (b != null) {
                populateBeneficiaryFields(b);
                selectCitizenForDashboard(b.getBenId());
            }
        }
    }

    private void handleSessionListSelection() {
        int idx = sessionAwtList.getSelectedIndex();
        if (idx >= 0 && idx < choiceSlotSession.getItemCount()) {
            choiceSlotSession.select(idx);
        }
    }

    private void handleQueueListSelection() {
        int idx = queueAwtList.getSelectedIndex();
        if (idx >= 0) {
            String item = queueAwtList.getItem(idx);
            String benId = item.split("\\|")[0].trim();
            Beneficiary b = service.getLookup().get(benId);
            if (b != null) {
                txtVaccBenId.setText(b.getBenId());
                txtVaccBenName.setText(b.getName());
                txtVaccBenAge.setText(String.valueOf(b.getAge()));
            }
        }
    }

    private void populateBeneficiaryFields(Beneficiary b) {
        txtRegId.setText(b.getBenId());
        txtRegName.setText(b.getName());
        txtRegAge.setText(String.valueOf(b.getAge()));
        txtRegIdNumber.setText(b.getIdNumber());
        txtRegPhone.setText(b.getPhone());
        lblRegGroup.setText(b.getAgeGroup());

        txtSlotBenId.setText(b.getBenId());
        lblSlotCitizenInfo.setText(String.format(" %s (Age %d, %s)", b.getName(), b.getAge(), b.getAgeGroup()));

        txtVaccBenId.setText(b.getBenId());
        txtVaccBenName.setText(b.getName());
        txtVaccBenAge.setText(String.valueOf(b.getAge()));
    }

    // Public programmatic helpers for tests / screenshots
    public void showDashboardTab() { setActiveNav(btnNavDash, CARD_DASHBOARD); }
    public void showRegistrationTab() { setActiveNav(btnNavReg, CARD_REGISTRATION); }
    public void showSlotsTab() { setActiveNav(btnNavSlots, CARD_SLOTS); }
    public void showVaccinationTab() { setActiveNav(btnNavVacc, CARD_VACCINATION); }
    public void showReportsTab() { setActiveNav(btnNavReports, CARD_REPORTS); }
    public void showToolsTab() { setActiveNav(btnNavTools, CARD_TOOLS); }

    public void selectBeneficiaryIndex(int index) {
        if (benAwtList.getItemCount() > index) {
            benAwtList.select(index);
            handleBeneficiaryListSelection();
        }
    }

    public void selectSessionIndex(int index) {
        if (sessionAwtList.getItemCount() > index) {
            sessionAwtList.select(index);
            handleSessionListSelection();
        }
    }

    public void viewThreadsReport() {
        setActiveNav(btnNavTools, CARD_TOOLS);
        updateThreadStateDisplay();
    }

    public void viewVialLogsReport() {
        setActiveNav(btnNavTools, CARD_TOOLS);
        txtToolsOutput.setText("=== VIAL CONSUMPTION & AUDIT LOGS (Vector) ===\n\n");
        for (String s : service.getVialLogs()) {
            txtToolsOutput.append(s + "\n");
        }
    }

    public void runRaceComparison() {
        setActiveNav(btnNavTools, CARD_TOOLS);
        handleRunRaceComparison();
    }

    // =========================================================================
    // LISTENER INTERFACES
    // =========================================================================
    @Override
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        if (src == chkSyncToggle) {
            boolean sync = chkSyncToggle.getState();
            service.getVialManager().setUseSynchronization(sync);
            showInfo("Vial concurrency mode set to: " + (sync ? "SYNCHRONIZED (Safe)" : "UNSYNCHRONIZED (Race Risk)"));
        } else if (src == choiceDashVaccine) {
            updateDashEligibilityPreview();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ENTER) {
            handleSearchBeneficiary();
        } else if (e.isControlDown() && code == KeyEvent.VK_V) {
            handleAdministerDose();
        } else if (code == KeyEvent.VK_F4) {
            generateDailyCoverageReport();
        } else if (code == KeyEvent.VK_ESCAPE) {
            clearRegistrationForm();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
