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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Pure AWT Graphical User Interface for VaxiCare.
 * STRICTLY uses java.awt and java.awt.event (NO javax.swing.*).
 * Demonstrates Unit IV: AWT Components, Layout Managers (CardLayout, BorderLayout, GridLayout),
 * Delegation Event Model (ActionListener, ItemListener, KeyListener), Adapter classes (WindowAdapter, MouseAdapter),
 * and exhaustive exception handling dialogs.
 */
public class VaxiCareFrame extends Frame implements ActionListener, ItemListener, KeyListener {

    private final VaxiCareService service;

    // Card Layout and Container
    private CardLayout cardLayout;
    private Panel cardsPanel;

    // Card identifiers
    private static final String CARD_REGISTRATION = "REGISTRATION";
    private static final String CARD_SLOTS = "SLOTS";
    private static final String CARD_VACCINATION = "VACCINATION";
    private static final String CARD_REPORTS = "REPORTS";

    // Navigational Buttons
    private Button btnNavReg;
    private Button btnNavSlots;
    private Button btnNavVacc;
    private Button btnNavReports;

    // 1. REGISTRATION PANEL COMPONENTS
    private TextField txtRegId;
    private TextField txtRegName;
    private TextField txtRegAge;
    private TextField txtRegIdNumber;
    private TextField txtRegPhone;
    private Label lblRegGroup;
    private Button btnRegister;
    private Button btnSearchBen;
    private Button btnClearReg;
    private java.awt.List benAwtList;

    // 2. SLOTS PANEL COMPONENTS
    private TextField txtSlotBenId;
    private Choice choiceSlotSession;
    private Choice choiceSlotVaccine;
    private Button btnBookSlot;
    private Button btnAutoBook;
    private Button btnRefreshSessions;
    private java.awt.List sessionAwtList;

    // 3. VACCINATION PANEL COMPONENTS
    private TextField txtVaccBenId;
    private TextField txtVaccBenName;
    private TextField txtVaccBenAge;
    private Choice choiceVaccBrand;
    private TextField txtVaccSessionId;
    private Checkbox chkSyncToggle;
    private Button btnAdministerDose;
    private Button btnSimulateRace;
    private Button btnBackupBinary;
    private Button btnRestoreBinary;
    private Label lblVialStatus;
    private java.awt.List queueAwtList;

    // 4. REPORTS PANEL COMPONENTS
    private TextArea txtReportDisplay;
    private Button btnGenDailyReport;
    private Button btnViewReminders;
    private Button btnViewThreads;
    private Button btnViewVialLogs;
    private Button btnTriggerUncaught;

    public VaxiCareFrame(VaxiCareService service) {
        super("VaxiCare - Smart Vaccination Slot Booking & Dose Tracking System");
        this.service = service;

        initializeUI();
        refreshAllData();

        // Start background worker threads
        service.startBackgroundThreads();
    }

    private void initializeUI() {
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(new Color(240, 244, 248));

        // 1. Header & Navigation Bar
        Panel topPanel = new Panel(new BorderLayout());
        topPanel.setBackground(new Color(24, 43, 73));

        Label headerTitle = new Label("  VaxiCare 2.0: Smart Clinical Vaccination System", Label.LEFT);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        headerTitle.setForeground(Color.WHITE);
        topPanel.add(headerTitle, BorderLayout.NORTH);

        Panel navPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        navPanel.setBackground(new Color(38, 67, 115));

        btnNavReg = createNavButton("1. Registration");
        btnNavSlots = createNavButton("2. Slot Booking");
        btnNavVacc = createNavButton("3. Vaccination & Vials (Ctrl+V)");
        btnNavReports = createNavButton("4. Reports & Logs (F4)");

        navPanel.add(btnNavReg);
        navPanel.add(btnNavSlots);
        navPanel.add(btnNavVacc);
        navPanel.add(btnNavReports);
        topPanel.add(navPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // 2. Central CardLayout Area
        cardLayout = new CardLayout();
        cardsPanel = new Panel(cardLayout);

        cardsPanel.add(createRegistrationPanel(), CARD_REGISTRATION);
        cardsPanel.add(createSlotsPanel(), CARD_SLOTS);
        cardsPanel.add(createVaccinationPanel(), CARD_VACCINATION);
        cardsPanel.add(createReportsPanel(), CARD_REPORTS);

        add(cardsPanel, BorderLayout.CENTER);

        // 3. Status Bar at Bottom
        Panel statusPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(220, 225, 230));
        Label statusLabel = new Label("Shortcuts: [ENTER] Fetch Beneficiary | [Ctrl+V] Administer Dose | [F4] Coverage Report | Ready.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        // 4. WindowAdapter for clean shutdown
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("[VaxiCareFrame] Application closing. Terminating background threads...");
                service.stopBackgroundThreads();
                dispose();
                System.exit(0);
            }
        });

        // Global Key Listener for frame
        addKeyListener(this);
        setFocusable(true);
    }

    private Button createNavButton(String label) {
        Button btn = new Button(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(new Color(225, 235, 245));
        btn.addActionListener(this);
        btn.addKeyListener(this);
        return btn;
    }

    // ==========================================
    // PANEL 1: REGISTRATION PANEL
    // ==========================================
    private Panel createRegistrationPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 248, 252));

        // Form Panel (Left)
        Panel formPanel = new Panel(new GridLayout(8, 2, 8, 8));
        formPanel.setBackground(new Color(245, 248, 252));

        txtRegId = new TextField(12);
        txtRegId.addKeyListener(this);
        txtRegName = new TextField(15);
        txtRegName.addKeyListener(this);
        txtRegAge = new TextField(5);
        txtRegAge.addKeyListener(this);
        txtRegIdNumber = new TextField(15);
        txtRegIdNumber.addKeyListener(this);
        txtRegPhone = new TextField(12);
        txtRegPhone.addKeyListener(this);
        lblRegGroup = new Label("Calculated on Age");

        btnRegister = new Button("Register New Beneficiary");
        btnRegister.setBackground(new Color(40, 140, 70));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.addActionListener(this);

        btnSearchBen = new Button("Search by Ben ID (Enter)");
        btnSearchBen.addActionListener(this);

        btnClearReg = new Button("Clear Form");
        btnClearReg.addActionListener(this);

        formPanel.add(new Label("Beneficiary ID:"));
        formPanel.add(txtRegId);
        formPanel.add(new Label("Full Name:"));
        formPanel.add(txtRegName);
        formPanel.add(new Label("Age (Years):"));
        formPanel.add(txtRegAge);
        formPanel.add(new Label("Identity Document #:"));
        formPanel.add(txtRegIdNumber);
        formPanel.add(new Label("Mobile Phone (10 digits):"));
        formPanel.add(txtRegPhone);
        formPanel.add(new Label("Demographic Group:"));
        formPanel.add(lblRegGroup);
        formPanel.add(btnRegister);
        formPanel.add(btnSearchBen);
        formPanel.add(btnClearReg);
        formPanel.add(new Label(""));

        // List Panel (Right)
        Panel listPanel = new Panel(new BorderLayout(5, 5));
        listPanel.add(new Label("Registered Beneficiaries (Click row to select):", Label.LEFT), BorderLayout.NORTH);
        benAwtList = new java.awt.List(15, false);
        benAwtList.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // MouseAdapter for row selection
        benAwtList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBeneficiaryListSelection();
            }
        });
        benAwtList.addKeyListener(this);
        listPanel.add(benAwtList, BorderLayout.CENTER);

        panel.add(formPanel, BorderLayout.WEST);
        panel.add(listPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // PANEL 2: SLOTS PANEL
    // ==========================================
    private Panel createSlotsPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 248, 252));

        Panel formPanel = new Panel(new GridLayout(6, 2, 8, 8));
        txtSlotBenId = new TextField(12);
        txtSlotBenId.addKeyListener(this);

        choiceSlotSession = new Choice();
        choiceSlotSession.addItemListener(this);

        choiceSlotVaccine = new Choice();
        choiceSlotVaccine.add("Covishield");
        choiceSlotVaccine.add("Covaxin");
        choiceSlotVaccine.add("ChildCare");
        choiceSlotVaccine.add("BoosterCare");

        btnBookSlot = new Button("Book Selected Slot");
        btnBookSlot.setBackground(new Color(30, 110, 190));
        btnBookSlot.setForeground(Color.WHITE);
        btnBookSlot.addActionListener(this);

        btnAutoBook = new Button("Auto-Match First Available");
        btnAutoBook.addActionListener(this);

        btnRefreshSessions = new Button("Refresh Sessions List");
        btnRefreshSessions.addActionListener(this);

        formPanel.add(new Label("Beneficiary ID:"));
        formPanel.add(txtSlotBenId);
        formPanel.add(new Label("Select Session:"));
        formPanel.add(choiceSlotSession);
        formPanel.add(new Label("Vaccine Product:"));
        formPanel.add(choiceSlotVaccine);
        formPanel.add(btnBookSlot);
        formPanel.add(btnAutoBook);
        formPanel.add(btnRefreshSessions);
        formPanel.add(new Label(""));

        // Sessions List Display
        Panel listPanel = new Panel(new BorderLayout(5, 5));
        listPanel.add(new Label("Active Vaccination Center Sessions:", Label.LEFT), BorderLayout.NORTH);
        sessionAwtList = new java.awt.List(15, false);
        sessionAwtList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        sessionAwtList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleSessionListSelection();
            }
        });
        listPanel.add(sessionAwtList, BorderLayout.CENTER);

        panel.add(formPanel, BorderLayout.WEST);
        panel.add(listPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // PANEL 3: VACCINATION & VIALS PANEL
    // ==========================================
    private Panel createVaccinationPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 248, 252));

        Panel formPanel = new Panel(new GridLayout(9, 2, 6, 6));
        txtVaccBenId = new TextField(12);
        txtVaccBenId.addKeyListener(this);
        txtVaccBenName = new TextField(15);
        txtVaccBenAge = new TextField(5);
        txtVaccSessionId = new TextField("1", 5);

        choiceVaccBrand = new Choice();
        choiceVaccBrand.add("Covishield");
        choiceVaccBrand.add("Covaxin");
        choiceVaccBrand.add("ChildCare");
        choiceVaccBrand.add("BoosterCare");

        chkSyncToggle = new Checkbox("Enable Thread Locking", service.getVialManager().isUseSynchronization());
        chkSyncToggle.addItemListener(this);

        btnAdministerDose = new Button("Administer Dose (Ctrl+V)");
        btnAdministerDose.setBackground(new Color(180, 40, 40));
        btnAdministerDose.setForeground(Color.WHITE);
        btnAdministerDose.addActionListener(this);

        btnSimulateRace = new Button("Simulate Concurrency Race");
        btnSimulateRace.addActionListener(this);

        btnBackupBinary = new Button("Backup Doses (doses.dat)");
        btnBackupBinary.addActionListener(this);

        btnRestoreBinary = new Button("Restore & Verify doses.dat");
        btnRestoreBinary.addActionListener(this);

        lblVialStatus = new Label("Vials Opened: 0 | Left: 0 | Wasted: 0");
        lblVialStatus.setFont(new Font("SansSerif", Font.BOLD, 12));

        formPanel.add(new Label("Beneficiary ID:"));
        formPanel.add(txtVaccBenId);
        formPanel.add(new Label("Name:"));
        formPanel.add(txtVaccBenName);
        formPanel.add(new Label("Age:"));
        formPanel.add(txtVaccBenAge);
        formPanel.add(new Label("Vaccine:"));
        formPanel.add(choiceVaccBrand);
        formPanel.add(new Label("Session ID:"));
        formPanel.add(txtVaccSessionId);
        formPanel.add(new Label("Vial Concurrency Mode:"));
        formPanel.add(chkSyncToggle);
        formPanel.add(btnAdministerDose);
        formPanel.add(btnSimulateRace);
        formPanel.add(btnBackupBinary);
        formPanel.add(btnRestoreBinary);

        // Queue Panel (Right)
        Panel queuePanel = new Panel(new BorderLayout(5, 5));
        queuePanel.add(new Label("Vaccination Queue (LinkedList - Click row to load):", Label.LEFT), BorderLayout.NORTH);
        queueAwtList = new java.awt.List(12, false);
        queueAwtList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        queueAwtList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleQueueListSelection();
            }
        });
        queuePanel.add(queueAwtList, BorderLayout.CENTER);
        queuePanel.add(lblVialStatus, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.WEST);
        panel.add(queuePanel, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // PANEL 4: REPORTS & LOGS PANEL
    // ==========================================
    private Panel createReportsPanel() {
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 248, 252));

        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnGenDailyReport = new Button("Daily Coverage Report (F4)");
        btnGenDailyReport.addActionListener(this);

        btnViewReminders = new Button("Due Tomorrow Reminders");
        btnViewReminders.addActionListener(this);

        btnViewThreads = new Button("Thread State Monitor");
        btnViewThreads.addActionListener(this);

        btnViewVialLogs = new Button("Vial Audit Logs");
        btnViewVialLogs.addActionListener(this);

        btnTriggerUncaught = new Button("Trigger Uncaught Exception");
        btnTriggerUncaught.setBackground(new Color(200, 70, 70));
        btnTriggerUncaught.setForeground(Color.WHITE);
        btnTriggerUncaught.addActionListener(this);

        btnPanel.add(btnGenDailyReport);
        btnPanel.add(btnViewReminders);
        btnPanel.add(btnViewThreads);
        btnPanel.add(btnViewVialLogs);
        btnPanel.add(btnTriggerUncaught);

        txtReportDisplay = new TextArea("", 20, 80, TextArea.SCROLLBARS_BOTH);
        txtReportDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReportDisplay.setEditable(false);
        txtReportDisplay.addKeyListener(this);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(txtReportDisplay, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // DATA REFRESH LOGIC
    // ==========================================
    public void refreshAllData() {
        try {
            // Refresh Beneficiaries list
            benAwtList.removeAll();
            List<Beneficiary> bens = service.getBeneficiaryDAO().findAll();
            for (Beneficiary b : bens) {
                benAwtList.add(String.format("%-7s | %-16s | Age: %2d | %-20s | Ph: %s",
                        b.getBenId(), b.getName(), b.getAge(), b.getAgeGroup(), b.getPhone()));
            }

            // Refresh Sessions list & dropdown
            sessionAwtList.removeAll();
            choiceSlotSession.removeAll();
            List<Session> sess = service.getSessionDAO().findAll();
            for (Session s : sess) {
                String line = String.format("Sess #%-2d | %s | %-11s | Slots: %2d/%2d (%2d free)",
                        s.getSessionId(), s.getSessionDate(), s.getVaccineName(), s.getBooked(), s.getCapacity(), s.getAvailableSlots());
                sessionAwtList.add(line);
                choiceSlotSession.add("Session #" + s.getSessionId() + " (" + s.getSessionDate() + " - " + s.getVaccineName() + ")");
            }

            // Refresh Queue
            queueAwtList.removeAll();
            synchronized (service.getVaccinationQueue()) {
                for (Beneficiary qb : service.getVaccinationQueue()) {
                    queueAwtList.add(String.format("%-7s | %-16s | Age: %2d | %s",
                            qb.getBenId(), qb.getName(), qb.getAge(), qb.getAgeGroup()));
                }
            }

            // Refresh Vial Status Label
            lblVialStatus.setText(String.format("Vials Opened: %d | Left in Current Vial: %d | Wasted Doses: %d",
                    service.getVialManager().getVialsOpened(),
                    service.getVialManager().getDosesRemainingInCurrentVial(),
                    service.getVialManager().getDosesWasted()));

        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to refresh system data:\n" + e.getMessage());
        }
    }

    // ==========================================
    // EVENT HANDLERS (MOUSE & LIST SELECTION)
    // ==========================================
    private void handleBeneficiaryListSelection() {
        int index = benAwtList.getSelectedIndex();
        if (index >= 0) {
            String item = benAwtList.getItem(index);
            String benId = item.split("\\|")[0].trim();
            Beneficiary b = service.getLookup().get(benId);
            if (b != null) {
                populateBeneficiaryFields(b);
            }
        }
    }

    private void handleSessionListSelection() {
        int index = sessionAwtList.getSelectedIndex();
        if (index >= 0 && index < choiceSlotSession.getItemCount()) {
            choiceSlotSession.select(index);
        }
    }

    private void handleQueueListSelection() {
        int index = queueAwtList.getSelectedIndex();
        if (index >= 0) {
            String item = queueAwtList.getItem(index);
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
        txtVaccBenId.setText(b.getBenId());
        txtVaccBenName.setText(b.getName());
        txtVaccBenAge.setText(String.valueOf(b.getAge()));
    }

    // ==========================================
    // ACTION LISTENER DISPATCH
    // ==========================================
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        // 1. Navigation Actions
        if (src == btnNavReg) {
            cardLayout.show(cardsPanel, CARD_REGISTRATION);
        } else if (src == btnNavSlots) {
            cardLayout.show(cardsPanel, CARD_SLOTS);
        } else if (src == btnNavVacc) {
            cardLayout.show(cardsPanel, CARD_VACCINATION);
        } else if (src == btnNavReports) {
            cardLayout.show(cardsPanel, CARD_REPORTS);
        }
        // 2. Registration Actions
        else if (src == btnRegister) {
            handleRegisterBeneficiary();
        } else if (src == btnSearchBen) {
            handleSearchBeneficiary();
        } else if (src == btnClearReg) {
            clearRegistrationForm();
        }
        // 3. Slot Booking Actions
        else if (src == btnBookSlot) {
            handleBookSlotExplicit();
        } else if (src == btnAutoBook) {
            handleAutoBookSlot();
        } else if (src == btnRefreshSessions) {
            refreshAllData();
        }
        // 4. Vaccination Actions
        else if (src == btnAdministerDose) {
            handleAdministerDose();
        } else if (src == btnSimulateRace) {
            handleSimulateRace();
        } else if (src == btnBackupBinary) {
            handleBackupBinary();
        } else if (src == btnRestoreBinary) {
            handleRestoreBinary();
        }
        // 5. Reports Actions
        else if (src == btnGenDailyReport) {
            generateDailyCoverageReport();
        } else if (src == btnViewReminders) {
            viewRemindersReport();
        } else if (src == btnViewThreads) {
            viewThreadsReport();
        } else if (src == btnViewVialLogs) {
            viewVialLogsReport();
        } else if (src == btnTriggerUncaught) {
            // Deliberately triggers an ArithmeticException for syllabus demo
            triggerUncaughtException();
        }
    }

    // ==========================================
    // ACTION IMPLEMENTATIONS WITH EXCEPTION CATCHING
    // ==========================================

    private void handleRegisterBeneficiary() {
        try {
            String name = txtRegName.getText().trim();
            if (name.isEmpty()) {
                throw new VaxiCareException("Beneficiary name cannot be blank.");
            }

            int age = Integer.parseInt(txtRegAge.getText().trim());
            String idNum = txtRegIdNumber.getText().trim();
            String phone = txtRegPhone.getText().trim();

            if (!Beneficiary.isValidPhone(phone)) {
                throw new VaxiCareException("Invalid phone number! Must be exactly 10 digits.");
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
                showInfoDialog("Registration Successful", "Beneficiary successfully registered:\n" + b);
            }

        } catch (NumberFormatException nfe) {
            showErrorDialog("Invalid Age", "Age must be a valid integer number! Details: " + nfe.getMessage());
        } catch (SQLException sqle) {
            showErrorDialog("Database Error", "Failed to insert beneficiary into database:\n" + sqle.getMessage());
        } catch (VaxiCareException ve) {
            showErrorDialog("Validation Error", ve.getMessage());
        } catch (Exception ex) {
            showErrorDialog("System Error", "Unexpected error: " + ex.getMessage());
        }
    }

    private void handleSearchBeneficiary() {
        try {
            String benId = txtRegId.getText().trim();
            if (benId.isEmpty()) {
                benId = txtSlotBenId.getText().trim();
            }
            if (benId.isEmpty()) {
                benId = txtVaccBenId.getText().trim();
            }
            if (benId.isEmpty()) {
                throw new VaxiCareException("Please enter a Beneficiary ID to search (e.g., 'B5531').");
            }

            Beneficiary b = service.getLookup().get(benId);
            if (b == null) {
                b = service.getBeneficiaryDAO().findById(benId);
                if (b != null) {
                    service.getLookup().put(benId, b);
                }
            }

            if (b == null) {
                showErrorDialog("Not Found", "Beneficiary ID '" + benId + "' was not found in the database.");
            } else {
                populateBeneficiaryFields(b);
                showInfoDialog("Beneficiary Found", "Found Record:\n" + b);
            }

        } catch (SQLException sqle) {
            showErrorDialog("Database Error", sqle.getMessage());
        } catch (VaxiCareException ve) {
            showErrorDialog("Lookup Error", ve.getMessage());
        }
    }

    private void clearRegistrationForm() {
        txtRegId.setText("");
        txtRegName.setText("");
        txtRegAge.setText("");
        txtRegIdNumber.setText("");
        txtRegPhone.setText("");
        lblRegGroup.setText("Calculated on Age");
    }

    private void handleBookSlotExplicit() {
        try {
            String benId = txtSlotBenId.getText().trim();
            if (benId.isEmpty()) {
                throw new VaxiCareException("Beneficiary ID cannot be empty.");
            }

            int selectedIdx = choiceSlotSession.getSelectedIndex();
            if (selectedIdx < 0 || selectedIdx >= service.getSessions().size()) {
                throw new VaxiCareException("Please select a valid vaccination session.");
            }

            Session target = service.getSessions().get(selectedIdx);
            String vaxBrand = choiceSlotVaccine.getSelectedItem();

            // Calls Overloaded Method 3: bookSlot(benId, sessionId, vaccineName)
            service.bookSlot(benId, target.getSessionId(), vaxBrand);
            refreshAllData();
            showInfoDialog("Slot Booked", String.format("Slot confirmed for %s at Session #%d (%s) with %s.",
                    benId, target.getSessionId(), target.getSessionDate(), vaxBrand));

        } catch (DoseNotDueException dnde) {
            showErrorDialog("Dose Not Due", dnde.getMessage());
        } catch (SessionFullException sfe) {
            showErrorDialog("Session Full", sfe.getMessage());
        } catch (IneligibleAgeException iae) {
            showErrorDialog("Ineligible Beneficiary", iae.getMessage());
        } catch (SQLException sqle) {
            showErrorDialog("Database Error", sqle.getMessage());
        } catch (VaxiCareException ve) {
            showErrorDialog("Booking Error", ve.getMessage());
        }
    }

    private void handleAutoBookSlot() {
        try {
            String benId = txtSlotBenId.getText().trim();
            if (benId.isEmpty()) {
                throw new VaxiCareException("Please enter a Beneficiary ID for auto-matching.");
            }

            // Calls Overloaded Method 1: bookSlot(benId)
            service.bookSlot(benId);
            refreshAllData();
            showInfoDialog("Auto-Match Success", "Beneficiary " + benId + " auto-scheduled to first available session!");

        } catch (DoseNotDueException dnde) {
            showErrorDialog("Dose Not Due", dnde.getMessage());
        } catch (SessionFullException sfe) {
            showErrorDialog("Session Full", sfe.getMessage());
        } catch (IneligibleAgeException iae) {
            showErrorDialog("Ineligible Beneficiary", iae.getMessage());
        } catch (SQLException sqle) {
            showErrorDialog("Database Error", sqle.getMessage());
        } catch (VaxiCareException ve) {
            showErrorDialog("Booking Error", ve.getMessage());
        }
    }

    private void handleAdministerDose() {
        try {
            String benId = txtVaccBenId.getText().trim();
            if (benId.isEmpty()) {
                throw new VaxiCareException("Beneficiary ID is required to administer dose.");
            }
            String vaxName = choiceVaccBrand.getSelectedItem();
            int sessId = 1;
            try {
                sessId = Integer.parseInt(txtVaccSessionId.getText().trim());
            } catch (NumberFormatException ignored) {
            }

            DoseRecord rec = service.recordVaccination(benId, vaxName, sessId);
            refreshAllData();
            showInfoDialog("Dose Administered", String.format(
                    "Dose successfully administered!\nBeneficiary: %s\nVaccine: %s (Dose #%d)\nCertificate: %s\nNext Due: %s",
                    rec.getBenId(), rec.getVaccineName(), rec.getDoseNo(), rec.getCertificateNo(), rec.getNextDue()));

        } catch (IneligibleAgeException iae) {
            showErrorDialog("Ineligible Candidate", iae.getMessage());
        } catch (SQLException sqle) {
            showErrorDialog("Database Error", sqle.getMessage());
        } catch (VaxiCareException ve) {
            showErrorDialog("Clinical Error", ve.getMessage());
        }
    }

    private void handleSimulateRace() {
        txtReportDisplay.setText("=== Concurrency Demonstration: Drawing 15 Doses ===\n");
        boolean sync = service.getVialManager().isUseSynchronization();
        txtReportDisplay.append("Current Concurrency Mode: " + (sync ? "SAFE SYNCHRONIZED" : "UNSYNCHRONIZED (RACE RISK)") + "\n\n");

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 8; i++) {
                service.getVialManager().drawDose("Worker-T1");
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 7; i++) {
                service.getVialManager().drawDose("Worker-T2");
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException ignored) {
        }

        refreshAllData();
        cardLayout.show(cardsPanel, CARD_REPORTS);
        viewVialLogsReport();
    }

    private void handleBackupBinary() {
        try {
            service.backupDosesToBinary("doses.dat");
            showInfoDialog("Backup Complete", "Doses successfully exported to binary 'doses.dat' via FileOutputStream.");
        } catch (IOException ioe) {
            showErrorDialog("I/O Backup Error", "Failed to write binary backup:\n" + ioe.getMessage());
        } catch (SQLException sqle) {
            showErrorDialog("Database Error", sqle.getMessage());
        }
    }

    private void handleRestoreBinary() {
        try {
            boolean matched = service.restoreDosesAndVerify("doses.dat");
            if (matched) {
                showInfoDialog("Verification Verified (100% Match)",
                        "Restored records from 'doses.dat' via FileInputStream.\nExact record count matches SQLite database!");
            } else {
                showErrorDialog("Verification Mismatch", "Count mismatch between binary backup and database!");
            }
        } catch (IOException ioe) {
            showErrorDialog("I/O Restore Error", "Failed to read binary backup:\n" + ioe.getMessage());
        } catch (SQLException sqle) {
            showErrorDialog("Database Error", sqle.getMessage());
        }
    }

    public void generateDailyCoverageReport() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================================================\n");
            sb.append("                       VAXICARE DAILY VACCINATION COVERAGE REPORT                       \n");
            sb.append("========================================================================================\n");
            sb.append("Generated On: ").append(LocalDate.now()).append("\n\n");

            int totalBens = service.getBeneficiaryDAO().count();
            int totalDoses = service.getDoseDAO().count();
            int totalSess = service.getSessionDAO().count();

            sb.append(String.format("1. SUMMARY METRICS:\n"));
            sb.append(String.format("   - Total Registered Beneficiaries : %d\n", totalBens));
            sb.append(String.format("   - Total Doses Administered       : %d\n", totalDoses));
            sb.append(String.format("   - Active Vaccination Sessions    : %d\n", totalSess));
            sb.append(String.format("   - Beneficiaries Vaccinated Today : %d\n", service.getVaccinatedToday().size()));
            sb.append(String.format("   - Physical Vials Opened          : %d\n", service.getVialManager().getVialsOpened()));
            sb.append(String.format("   - Doses Remaining in Current Vial: %d\n", service.getVialManager().getDosesRemainingInCurrentVial()));
            sb.append(String.format("   - Total Wasted Doses             : %d\n\n", service.getVialManager().getDosesWasted()));

            sb.append("2. SESSIONS CAPACITY UTILIZATION (Primitive Array Inspection):\n");
            int[] slotsRemaining = service.getSlotsRemainingPerSession();
            List<Session> sessList = service.getSessions();
            for (int i = 0; i < sessList.size(); i++) {
                Session s = sessList.get(i);
                int rem = (i < slotsRemaining.length) ? slotsRemaining[i] : s.getAvailableSlots();
                sb.append(String.format("   [Index %2d] Session #%-2d (%s | %-11s) -> Booked: %2d/%2d | Slots Remaining: %2d\n",
                        i, s.getSessionId(), s.getSessionDate(), s.getVaccineName(), s.getBooked(), s.getCapacity(), rem));
            }

            sb.append("\n3. RECENT DOSES ADMINISTERED (SQLite Table: doses):\n");
            List<DoseRecord> allDoses = service.getDoseDAO().findAll();
            for (int i = Math.max(0, allDoses.size() - 10); i < allDoses.size(); i++) {
                DoseRecord d = allDoses.get(i);
                sb.append(String.format("   #%-3d | Ben: %-6s | %-11s | Dose %d | Given: %s | Next Due: %-16s | Cert: %s\n",
                        d.getDoseId(), d.getBenId(), d.getVaccineName(), d.getDoseNo(), d.getDoseDate(), d.getNextDue(), d.getCertificateNo()));
            }

            txtReportDisplay.setText(sb.toString());
            cardLayout.show(cardsPanel, CARD_REPORTS);

        } catch (SQLException e) {
            showErrorDialog("Report Error", e.getMessage());
        }
    }

    public void showRegistrationTab() {
        cardLayout.show(cardsPanel, CARD_REGISTRATION);
    }

    public void showSlotsTab() {
        cardLayout.show(cardsPanel, CARD_SLOTS);
    }

    public void showVaccinationTab() {
        cardLayout.show(cardsPanel, CARD_VACCINATION);
    }

    public void showReportsTab() {
        cardLayout.show(cardsPanel, CARD_REPORTS);
    }

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

    public void viewRemindersReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BENEFICIARIES SCHEDULED FOR NEXT DOSE TOMORROW ===\n");
        sb.append("Reference Date: ").append(LocalDate.now().plusDays(1)).append("\n\n");

        List<Beneficiary> due = service.getDueTomorrowBeneficiaries();
        sb.append("Total Beneficiaries Due Tomorrow: ").append(due.size()).append("\n\n");
        for (Beneficiary b : due) {
            sb.append(String.format(" -> [%-6s] %-18s | Age: %2d (%-20s) | Mobile: %s\n",
                    b.getBenId(), b.getName(), b.getAge(), b.getAgeGroup(), b.getPhone()));
        }

        txtReportDisplay.setText(sb.toString());
        cardLayout.show(cardsPanel, CARD_REPORTS);
    }

    public void viewThreadsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MULTITHREADING AUDIT & THREAD STATE MONITOR ===\n\n");
        List<String> logs = service.getReminderTask().getReminderLogs();
        if (logs.isEmpty()) {
            sb.append("No thread monitor events recorded yet. Background monitoring active.\n");
        } else {
            for (String log : logs) {
                sb.append(log).append("\n");
            }
        }
        txtReportDisplay.setText(sb.toString());
        cardLayout.show(cardsPanel, CARD_REPORTS);
    }

    public void viewVialLogsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== VIAL CONSUMPTION & AUDIT LOGS (Vector) ===\n\n");
        List<String> logs = service.getVialLogs();
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        txtReportDisplay.setText(sb.toString());
        cardLayout.show(cardsPanel, CARD_REPORTS);
    }

    private void triggerUncaughtException() {
        showInfoDialog("Triggering Uncaught Exception",
                "Calling com.vaxicare.exception.UncaughtDemo.main().\nCheck console output for uncaught ArithmeticException.");
        com.vaxicare.exception.UncaughtDemo.main(new String[0]);
    }

    // ==========================================
    // ITEM LISTENER (TOGGLE & SELECTION EVENTS)
    // ==========================================
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == chkSyncToggle) {
            boolean selected = chkSyncToggle.getState();
            service.getVialManager().setUseSynchronization(selected);
            showInfoDialog("Concurrency Mode", "Thread synchronization is now: " + (selected ? "ENABLED (Thread-Safe)" : "DISABLED (Race Conditions Possible)"));
        }
    }

    // ==========================================
    // KEY LISTENER (KEYBOARD SHORTCUTS)
    // ==========================================
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // 1. ENTER Shortcut: Fetches beneficiary by ID
        if (code == KeyEvent.VK_ENTER) {
            handleSearchBeneficiary();
        }
        // 2. Ctrl+V Shortcut: Administers dose
        else if (e.isControlDown() && code == KeyEvent.VK_V) {
            handleAdministerDose();
        }
        // 3. F4 Shortcut: Generates Daily Coverage Report
        else if (code == KeyEvent.VK_F4) {
            generateDailyCoverageReport();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    // ==========================================
    // AWT DIALOG HELPERS (STRICTLY NO SWING)
    // ==========================================
    public void showErrorDialog(String title, String message) {
        showAwtDialog("ERROR: " + title, message, new Color(180, 40, 40));
    }

    public void showInfoDialog(String title, String message) {
        showAwtDialog(title, message, new Color(30, 90, 160));
    }

    private void showAwtDialog(String title, String message, Color headerColor) {
        final Dialog dialog = new Dialog(this, title, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(440, 240);
        dialog.setLocationRelativeTo(this);

        Panel headerPanel = new Panel();
        headerPanel.setBackground(headerColor);
        Label lbl = new Label(title, Label.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        headerPanel.add(lbl);
        dialog.add(headerPanel, BorderLayout.NORTH);

        TextArea msgArea = new TextArea(message, 5, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
        msgArea.setEditable(false);
        msgArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dialog.add(msgArea, BorderLayout.CENTER);

        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        Button okBtn = new Button("   OK   ");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        btnPanel.add(okBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }
}
