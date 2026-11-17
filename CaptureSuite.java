import com.vaxicare.dao.DatabaseManager;
import com.vaxicare.model.Beneficiary;
import com.vaxicare.model.DoseRecord;
import com.vaxicare.service.VaxiCareService;
import com.vaxicare.ui.CertificateDialog;
import com.vaxicare.ui.VaxiCareFrame;

import javax.imageio.ImageIO;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class CaptureSuite {

    private static final String DIR = "screenshots";

    public static void main(String[] args) throws Exception {
        File dir = new File(DIR);
        if (!dir.exists()) dir.mkdirs();

        // 1. Ensure doses 1..11 have next_due set to tomorrow relative to current date
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE doses SET next_due = ? WHERE dose_id <= 11")) {
            ps.setString(1, tomorrow.toString());
            ps.executeUpdate();
        }

        System.out.println("[CaptureSuite] Initializing VaxiCare 2.0 service and frame...");
        VaxiCareService service = new VaxiCareService();

        // Draw doses to prime the vial gauge
        service.getVialManager().drawDoseSynchronized("Nurse-Priya");
        service.getVialManager().drawDoseSynchronized("Dr-Sharma");

        VaxiCareFrame frame = new VaxiCareFrame(service);
        frame.setSize(1180, 760);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Thread.sleep(1500);

        // 1. 01_dashboard_walkin.png — Dashboard & 1-Click Walk-In Wizard with top live counters
        System.out.println("[CaptureSuite] Capturing 01_dashboard_walkin.png...");
        frame.showDashboardTab();
        frame.selectCitizenForDashboard("B5531"); // Sarojini
        Thread.sleep(600);
        captureComponent(frame, DIR + "/01_dashboard_walkin.png");

        // 2. 02_citizen_registration.png — Citizen Registration with Name/ID search & B5531 (Sarojini) loaded
        System.out.println("[CaptureSuite] Capturing 02_citizen_registration.png...");
        frame.showRegistrationTab();
        frame.selectBeneficiaryIndex(24); // Sarojini
        Thread.sleep(600);
        captureComponent(frame, DIR + "/02_citizen_registration.png");

        // 3. 03_slot_booking_sessions.png — Slot Booking & Session Creator
        System.out.println("[CaptureSuite] Capturing 03_slot_booking_sessions.png...");
        frame.showSlotsTab();
        frame.selectSessionIndex(0);
        Thread.sleep(600);
        captureComponent(frame, DIR + "/03_slot_booking_sessions.png");

        // 4. 04_vaccination_vials.png — Administer Dose & Vial Lifecycle visual progress bar
        System.out.println("[CaptureSuite] Capturing 04_vaccination_vials.png...");
        Beneficiary sarojini = service.getLookup().get("B5531");
        Beneficiary aarav = service.getLookup().get("B5501");
        Beneficiary rohan = service.getLookup().get("B5503");
        if (sarojini != null) service.getVaccinationQueue().add(sarojini);
        if (aarav != null) service.getVaccinationQueue().add(aarav);
        if (rohan != null) service.getVaccinationQueue().add(rohan);

        frame.refreshAllData();
        frame.showVaccinationTab();
        Thread.sleep(600);
        captureComponent(frame, DIR + "/04_vaccination_vials.png");

        // 5. 05_dose_certificate.png — Printable Dose Certificate (VC-26-XXXX) modal dialog
        System.out.println("[CaptureSuite] Capturing 05_dose_certificate.png...");
        DoseRecord sampleDose = service.getDoseDAO().getLatestDose("B5531");
        if (sampleDose == null) {
            sampleDose = new DoseRecord(1, "B5531", "AdultShield", 1, LocalDate.now().toString(), LocalDate.now().plusDays(84).toString(), "VC-26-5531-01");
        }
        CertificateDialog certDialog = new CertificateDialog(frame, sampleDose, sarojini, service, false);
        certDialog.setSize(680, 560);
        certDialog.setLocationRelativeTo(frame);
        certDialog.setVisible(true);
        Thread.sleep(800);
        captureComponent(certDialog, DIR + "/05_dose_certificate.png");
        certDialog.dispose();

        // 6. 06_coverage_reminders.png — Daily Coverage Report (F4) & Iterator.remove() Reminder Purge
        System.out.println("[CaptureSuite] Capturing 06_coverage_reminders.png...");
        frame.generateDailyCoverageReport();
        Thread.sleep(600);
        captureComponent(frame, DIR + "/06_coverage_reminders.png");

        // 7. 07_threads_backup_lab.png — Thread Lifecycle (getState()), Sync Toggle & doses.dat Parity Lab
        System.out.println("[CaptureSuite] Capturing 07_threads_backup_lab.png...");
        frame.runRaceComparison();
        Thread.sleep(600);
        captureComponent(frame, DIR + "/07_threads_backup_lab.png");

        // 8. 08_exception_banner.png — Bottom color-coded status banner catching custom exception
        System.out.println("[CaptureSuite] Capturing 08_exception_banner.png...");
        frame.showSlotsTab();
        frame.showError("IneligibleAgeException: Beneficiary Sarojini [B5531, Age 64] is ineligible for vaccine 'ChildCare' (Pediatric criteria: 0-17 years only).");
        Thread.sleep(600);
        captureComponent(frame, DIR + "/08_exception_banner.png");

        System.out.println("[CaptureSuite] All 8 consecutive screenshots captured successfully in " + DIR + "/");

        frame.dispose();
        service.stopBackgroundThreads();
        System.exit(0);
    }

    private static void captureComponent(java.awt.Component comp, String filename) throws Exception {
        BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setColor(comp.getBackground());
        g2d.fillRect(0, 0, comp.getWidth(), comp.getHeight());
        comp.printAll(g2d);
        g2d.dispose();

        File file = new File(filename);
        ImageIO.write(img, "png", file);
        System.out.println("  -> Saved " + filename + " (" + file.length() + " bytes)");
    }
}
