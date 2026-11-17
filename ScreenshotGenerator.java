import com.vaxicare.dao.DatabaseManager;
import com.vaxicare.model.Beneficiary;
import com.vaxicare.service.VaxiCareService;
import com.vaxicare.ui.VaxiCareFrame;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

/**
 * Automates high-resolution screen capture across all 6 clinical workspaces of VaxiCare 2.0.
 */
public class ScreenshotGenerator {

    private static final String DIR = "screenshots";

    public static void main(String[] args) throws Exception {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 1. Ensure doses 1..11 have next_due set to tomorrow relative to current date
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE doses SET next_due = ? WHERE dose_id <= 11")) {
            ps.setString(1, tomorrow.toString());
            ps.executeUpdate();
        }

        System.out.println("[ScreenshotGenerator] Initializing VaxiCare 2.0 service and frame...");
        VaxiCareService service = new VaxiCareService();

        // Simulate clinical activity: draw doses to prime the vial gauge
        service.getVialManager().drawDoseSynchronized("Nurse-Priya");
        service.getVialManager().drawDoseSynchronized("Dr-Sharma");

        VaxiCareFrame frame = new VaxiCareFrame(service);
        frame.setSize(1180, 760);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Thread.sleep(1500);

        // 0. Capture Workspace 0: Dashboard & 1-Click Walk-In Wizard
        System.out.println("[ScreenshotGenerator] Capturing 00_dashboard_walkin.png...");
        frame.showDashboardTab();
        frame.selectCitizenForDashboard("B5531"); // Load Sarojini
        Thread.sleep(700);
        capture(frame, DIR + "/00_dashboard_walkin.png");

        // 1. Capture Workspace 1: Citizen Registration Panel
        System.out.println("[ScreenshotGenerator] Capturing 01_registration_panel.png...");
        frame.showRegistrationTab();
        frame.selectBeneficiaryIndex(24); // Sarojini (B5531)
        Thread.sleep(700);
        capture(frame, DIR + "/01_registration_panel.png");

        // 2. Capture Workspace 2: Slot Booking & Sessions Panel
        System.out.println("[ScreenshotGenerator] Capturing 02_slot_booking_panel.png...");
        frame.showSlotsTab();
        frame.selectSessionIndex(0);
        Thread.sleep(700);
        capture(frame, DIR + "/02_slot_booking_panel.png");

        // 3. Capture Workspace 3: Administer Dose & Vial Lifecycle Panel
        System.out.println("[ScreenshotGenerator] Capturing 03_vaccination_vials_panel.png...");
        // Add sample beneficiaries to queue for visual display
        Beneficiary sarojini = service.getLookup().get("B5531");
        Beneficiary aarav = service.getLookup().get("B5501");
        Beneficiary rohan = service.getLookup().get("B5503");
        if (sarojini != null) service.getVaccinationQueue().add(sarojini);
        if (aarav != null) service.getVaccinationQueue().add(aarav);
        if (rohan != null) service.getVaccinationQueue().add(rohan);

        frame.refreshAllData();
        frame.showVaccinationTab();
        Thread.sleep(700);
        capture(frame, DIR + "/03_vaccination_vials_panel.png");

        // 4. Capture Workspace 4: Daily Coverage Report
        System.out.println("[ScreenshotGenerator] Capturing 04_daily_coverage_report.png...");
        frame.generateDailyCoverageReport();
        Thread.sleep(700);
        capture(frame, DIR + "/04_daily_coverage_report.png");

        // 5. Capture Workspace 4 (Alt): Due Tomorrow Reminders & Iterator Purge
        System.out.println("[ScreenshotGenerator] Capturing 05_due_tomorrow_reminders.png...");
        frame.viewRemindersReport();
        Thread.sleep(700);
        capture(frame, DIR + "/05_due_tomorrow_reminders.png");

        // 6. Capture Workspace 5: Threads, Backup & Concurrency Race Comparison Lab
        System.out.println("[ScreenshotGenerator] Capturing 06_threads_tools.png...");
        frame.runRaceComparison();
        Thread.sleep(700);
        capture(frame, DIR + "/06_threads_tools.png");

        // 7. Capture Workspace 5 (Alt): Vial Audit Logs (Vector)
        System.out.println("[ScreenshotGenerator] Capturing 07_vial_audit_logs.png...");
        frame.viewVialLogsReport();
        Thread.sleep(700);
        capture(frame, DIR + "/07_vial_audit_logs.png");

        System.out.println("[ScreenshotGenerator] All screenshots captured successfully in " + DIR + "/");

        frame.dispose();
        service.stopBackgroundThreads();
        System.exit(0);
    }

    private static void capture(VaxiCareFrame frame, String filename) throws Exception {
        BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setColor(frame.getBackground());
        g2d.fillRect(0, 0, frame.getWidth(), frame.getHeight());
        frame.printAll(g2d);
        g2d.dispose();

        File file = new File(filename);
        ImageIO.write(img, "png", file);
        System.out.println("  -> Saved " + filename + " (" + file.length() + " bytes)");
    }
}
