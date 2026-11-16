package com.vaxicare.dao;

import com.vaxicare.model.Beneficiary;
import com.vaxicare.model.DoseRecord;
import com.vaxicare.model.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Auto-seeds database with initial sample data.
 * Demonstrates Unit I: java.util.Random, loops, arrays, and control flow.
 */
public class DataSeeder {

    private final BeneficiaryDAO beneficiaryDAO;
    private final SessionDAO sessionDAO;
    private final DoseDAO doseDAO;
    private final Random random;

    public DataSeeder() {
        this.beneficiaryDAO = new BeneficiaryDAO();
        this.sessionDAO = new SessionDAO();
        this.doseDAO = new DoseDAO();
        this.random = new Random(42); // Seeded for reproducibility
    }

    /**
     * Seeds initial records if database tables are currently empty.
     */
    public void seedIfEmpty() {
        try {
            int benCount = beneficiaryDAO.count();
            int sessCount = sessionDAO.count();
            int doseCount = doseDAO.count();

            if (benCount >= 22 && sessCount >= 20 && doseCount >= 22) {
                System.out.println("[DataSeeder] Database already contains sufficient records. Skipping seed.");
                return;
            }

            System.out.println("[DataSeeder] Empty or incomplete tables detected. Starting data population...");

            // 1. Seed Beneficiaries (at least 22 records, including Sarojini B5531)
            List<Beneficiary> beneficiaries = new ArrayList<>();

            // Explicit required record
            beneficiaries.add(new Beneficiary(
                    "B5531",
                    "Sarojini",
                    64,
                    "GOV-IND-5531",
                    "9876545531",
                    "Senior Citizen (60+)"
            ));

            String[] firstNames = {
                    "Aarav", "Ananya", "Rohan", "Pooja", "Vikram",
                    "Neha", "Aditya", "Ishita", "Suresh", "Meena",
                    "Deepak", "Sunita", "Kabir", "Diya", "Arjun",
                    "Kavita", "Rajesh", "Priyanka", "Amit", "Lakshmi",
                    "Manish", "Bhavna", "Kiran", "Sanjay"
            };

            int[] ages = {
                    8, 12, 15, 22, 28,
                    34, 40, 45, 52, 58,
                    61, 67, 72, 75, 10,
                    16, 25, 30, 48, 62,
                    65, 70, 29, 35
            };

            int counter = 5501;
            for (int i = 0; i < firstNames.length; i++) {
                String id = "B" + counter;
                if ("B5531".equals(id)) {
                    counter++;
                    id = "B" + counter;
                }
                String name = firstNames[i];
                int age = ages[i];
                String idNum = "GOV-ID-" + (1000 + random.nextInt(9000));
                String phone = "98" + String.format("%08d", 10000000 + random.nextInt(89999999));
                String group = Beneficiary.determineAgeGroup(age);

                beneficiaries.add(new Beneficiary(id, name, age, idNum, phone, group));
                counter++;
            }

            for (Beneficiary b : beneficiaries) {
                if (beneficiaryDAO.findById(b.getBenId()) == null) {
                    beneficiaryDAO.insert(b);
                }
            }
            System.out.println("[DataSeeder] Seeded " + beneficiaries.size() + " beneficiaries.");

            // 2. Seed Sessions (at least 20 records)
            String[] vaccines = {"Covishield", "Covaxin", "ChildCare", "BoosterCare"};
            LocalDate today = LocalDate.now();

            for (int i = 1; i <= 20; i++) {
                LocalDate sDate = today.plusDays((i - 1) % 10);
                String vax = vaccines[i % vaccines.length];
                int cap = 20 + random.nextInt(31); // 20 to 50
                int booked = random.nextInt(cap / 2); // some initial bookings

                Session s = new Session(i, sDate.toString(), vax, cap, booked);
                if (sessionDAO.findById(i) == null) {
                    sessionDAO.insert(s);
                }
            }
            System.out.println("[DataSeeder] Seeded 20 vaccination sessions.");

            // 3. Seed Doses (at least 22 records, exactly half due tomorrow)
            LocalDate tomorrow = today.plusDays(1);
            String tomorrowStr = tomorrow.toString();

            List<Beneficiary> savedBens = beneficiaryDAO.findAll();
            for (int i = 0; i < 22; i++) {
                Beneficiary b = savedBens.get(i % savedBens.size());
                String vax = (b.getAge() <= 17) ? "ChildCare" : ((b.getAge() >= 60) ? "BoosterCare" : "Covishield");
                int doseNo = (b.getAge() >= 60) ? 3 : (1 + (i % 2));
                LocalDate doseDate = today.minusDays(30 + random.nextInt(60));
                
                // Exactly half (11 doses) due tomorrow
                String nextDue;
                if (i < 11) {
                    nextDue = tomorrowStr;
                } else {
                    nextDue = today.plusDays(10 + random.nextInt(45)).toString();
                }

                String cert = "VC-26-" + (2000 + i);
                DoseRecord dr = new DoseRecord(i + 1, b.getBenId(), vax, doseNo, doseDate.toString(), nextDue, cert);
                if (doseDAO.findById(i + 1) == null) {
                    doseDAO.insert(dr);
                }
            }
            System.out.println("[DataSeeder] Seeded 22 dose records (11 due tomorrow).");

        } catch (SQLException e) {
            System.err.println("[DataSeeder] Error during seeding: " + e.getMessage());
        }
    }
}
