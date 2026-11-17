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
import java.util.List;

/**
 * Automates high-resolution screen capture across all tabs and workflows of VaxiCare.
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

        System.out.println("[ScreenshotGenerator] Initializing VaxiCare service and frame...");
        VaxiCareService service = new VaxiCareService();

        // Simulate clinical activity: draw a couple of doses
        service.getVialManager().drawDoseSynchronized("Nurse-Priya");
        service.getVialManager().drawDoseSynchronized("Dr-Sharma");

        VaxiCareFrame frame = new VaxiCareFrame(service);
        frame.setSize(1080, 720);
        frame.setVisible(true);

        Thread.sleep(1200);

        // 1. Capture Panel 1: Registration Panel with Sarojini (B5531) selected
        System.out.println("[ScreenshotGenerator] Capturing 01_registration_panel.png...");
        frame.showRegistrationTab();
        frame.selectBeneficiaryIndex(24); // Sarojini is item #25 (index 24)
        Thread.sleep(600);
        capture(frame, DIR + "/01_registration_panel.png");

        // 2. Capture Panel 2: Slot Booking Panel
        System.out.println("[ScreenshotGenerator] Capturing 02_slot_booking_panel.png...");
        frame.showSlotsTab();
        frame.selectSessionIndex(0); // Select first session
        Thread.sleep(600);
        capture(frame, DIR + "/02_slot_booking_panel.png");

        // 3. Capture Panel 3: Vaccination & Vials Panel with patients queued
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
        Thread.sleep(600);
        capture(frame, DIR + "/03_vaccination_vials_panel.png");

        // 4. Capture Panel 4: Daily Coverage Report
        System.out.println("[ScreenshotGenerator] Capturing 04_daily_coverage_report.png...");
        frame.generateDailyCoverageReport();
        Thread.sleep(600);
        capture(frame, DIR + "/04_daily_coverage_report.png");

        // 5. Capture Panel 4: Due Tomorrow Reminders
        System.out.println("[ScreenshotGenerator] Capturing 05_due_tomorrow_reminders.png...");
        frame.viewRemindersReport();
        Thread.sleep(600);
        capture(frame, DIR + "/05_due_tomorrow_reminders.png");

        // 6. Capture Panel 4: Thread State Monitor & Vial Logs
        System.out.println("[ScreenshotGenerator] Capturing 06_thread_monitor_audit.png...");
        // Add illustrative thread state logs if empty
        if (service.getReminderTask().getReminderLogs().isEmpty()) {
            service.getReminderTask().getReminderLogs().add("[12:50:00] [THREAD-INSPECTION] ReminderScanService: TIMED_WAITING | Vaccinator-Senior-Priority: WAITING (Priority 10) | Vaccinator-General: WAITING (Priority 5)");
            service.getReminderTask().getReminderLogs().add("[12:50:05] [REMINDER-SCAN] Tomorrow (" + tomorrow + "): 11 beneficiaries due for booster/next dose.");
            service.getReminderTask().getReminderLogs().add("  -> SMS Alert queued for Sarojini (B5531, Ph: 9876545531)");
            service.getReminderTask().getReminderLogs().add("  -> SMS Alert queued for Aarav (B5501, Ph: 9837392764)");
            service.getReminderTask().getReminderLogs().add("  -> SMS Alert queued for Ananya (B5502, Ph: 9822948885)");
        }
        frame.viewThreadsReport();
        Thread.sleep(600);
        capture(frame, DIR + "/06_thread_monitor_audit.png");

        // 7. Capture Panel 4: Vial Audit Logs
        System.out.println("[ScreenshotGenerator] Capturing 07_vial_audit_logs.png...");
        frame.viewVialLogsReport();
        Thread.sleep(600);
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
