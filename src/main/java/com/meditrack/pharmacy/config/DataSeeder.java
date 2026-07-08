package com.meditrack.pharmacy.config;

import com.meditrack.pharmacy.model.Medicine;
import com.meditrack.pharmacy.model.Role;
import com.meditrack.pharmacy.repository.MedicineRepository;
import com.meditrack.pharmacy.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final MedicineRepository medicineRepository;

    public DataSeeder(UserService userService, MedicineRepository medicineRepository) {
        this.userService = userService;
        this.medicineRepository = medicineRepository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedMedicines();
    }

    private void seedUsers() {
        if (userService.count() > 0) return;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MEDITRACK — Seeding default users");
        System.out.println("=".repeat(60));

        userService.createUser("owner", "ChangeMe123!", Role.OWNER, "system");
        userService.createUser("pharmacist", "ChangeMe123!", Role.PHARMACIST, "system");
        userService.createUser("staff", "ChangeMe123!", Role.STAFF, "system");

        System.out.println("  Created users: owner, pharmacist, staff");
        System.out.println("  Default password: ChangeMe123!");
        System.out.println();
        System.out.println("  ⚠  CHANGE THESE PASSWORDS IMMEDIATELY IN PRODUCTION!");
        System.out.println("=".repeat(60) + "\n");
    }

    private void seedMedicines() {
        if (medicineRepository.count() > 0) return;

        System.out.println("  Seeding sample medicines...");

        // 1. Already expired — demos expired-stock dashboard alert and sale-blocking
        Medicine expired = new Medicine();
        expired.setName("Amoxicillin 500mg");
        expired.setCategory("Antibiotic");
        expired.setManufacturer("PharmaCorp");
        expired.setBatchNumber("AMX-2024-001");
        expired.setExpiryDate(LocalDate.now().minusDays(30));
        expired.setPrice(new BigDecimal("12.99"));
        expired.setStockQuantity(50);
        expired.setReorderThreshold(20);
        expired.setPrescriptionRequired(true);
        medicineRepository.save(expired);

        // 2. Expiring within 15 days — demos near-expiry alert
        Medicine nearExpiry = new Medicine();
        nearExpiry.setName("Ibuprofen 400mg");
        nearExpiry.setCategory("Pain Relief");
        nearExpiry.setManufacturer("MediGen");
        nearExpiry.setBatchNumber("IBU-2025-042");
        nearExpiry.setExpiryDate(LocalDate.now().plusDays(15));
        nearExpiry.setPrice(new BigDecimal("8.49"));
        nearExpiry.setStockQuantity(15);
        nearExpiry.setReorderThreshold(20);
        nearExpiry.setPrescriptionRequired(false);
        medicineRepository.save(nearExpiry);

        // 3. Prescription-required — demos prescription enforcement
        Medicine prescription = new Medicine();
        prescription.setName("Metformin 850mg");
        prescription.setCategory("Diabetes");
        prescription.setManufacturer("HealthPlus");
        prescription.setBatchNumber("MET-2025-107");
        prescription.setExpiryDate(LocalDate.now().plusMonths(8));
        prescription.setPrice(new BigDecimal("15.75"));
        prescription.setStockQuantity(100);
        prescription.setReorderThreshold(25);
        prescription.setPrescriptionRequired(true);
        medicineRepository.save(prescription);

        // 4. Normal — good stock, future expiry
        Medicine normal1 = new Medicine();
        normal1.setName("Paracetamol 500mg");
        normal1.setCategory("Pain Relief");
        normal1.setManufacturer("GenericPharma");
        normal1.setBatchNumber("PCM-2025-203");
        normal1.setExpiryDate(LocalDate.now().plusYears(1));
        normal1.setPrice(new BigDecimal("5.99"));
        normal1.setStockQuantity(200);
        normal1.setReorderThreshold(30);
        normal1.setPrescriptionRequired(false);
        medicineRepository.save(normal1);

        // 5. Normal — good stock, future expiry
        Medicine normal2 = new Medicine();
        normal2.setName("Cetirizine 10mg");
        normal2.setCategory("Allergy");
        normal2.setManufacturer("AllerCare");
        normal2.setBatchNumber("CET-2025-089");
        normal2.setExpiryDate(LocalDate.now().plusMonths(10));
        normal2.setPrice(new BigDecimal("6.25"));
        normal2.setStockQuantity(150);
        normal2.setReorderThreshold(20);
        normal2.setPrescriptionRequired(false);
        medicineRepository.save(normal2);

        System.out.println("  Seeded 5 sample medicines (1 expired, 1 near-expiry, 1 Rx-required, 2 normal)");
    }
}
