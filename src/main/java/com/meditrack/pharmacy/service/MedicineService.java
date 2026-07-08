package com.meditrack.pharmacy.service;

import com.meditrack.pharmacy.model.Medicine;
import com.meditrack.pharmacy.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final AuditLogService auditLogService;

    public MedicineService(MedicineRepository medicineRepository, AuditLogService auditLogService) {
        this.medicineRepository = medicineRepository;
        this.auditLogService = auditLogService;
    }

    public List<Medicine> findAll() {
        return medicineRepository.findAll();
    }

    public Medicine findById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with id: " + id));
    }

    public List<Medicine> lowStock() {
        return medicineRepository.findAll().stream()
                .filter(Medicine::isLowStock)
                .collect(Collectors.toList());
    }

    public List<Medicine> nearExpiry() {
        return medicineRepository.findAll().stream()
                .filter(Medicine::isNearExpiry)
                .collect(Collectors.toList());
    }

    public List<Medicine> expired() {
        return medicineRepository.findAll().stream()
                .filter(Medicine::isExpired)
                .collect(Collectors.toList());
    }

    @Transactional
    public Medicine save(Medicine medicine, String actor) {
        boolean isNew = (medicine.getId() == null);
        Medicine saved = medicineRepository.save(medicine);
        String action = isNew ? "MEDICINE_CREATE" : "MEDICINE_UPDATE";
        auditLogService.log(actor, action,
                "Medicine: " + saved.getName() + " (ID: " + saved.getId() + "), Batch: " + saved.getBatchNumber());
        return saved;
    }

    @Transactional
    public void delete(Long id, String actor) {
        Medicine medicine = findById(id);
        auditLogService.log(actor, "MEDICINE_DELETE",
                "Deleted medicine: " + medicine.getName() + " (ID: " + id + "), Batch: " + medicine.getBatchNumber());
        medicineRepository.deleteById(id);
    }

    /**
     * Adjusts stock by the given delta (positive to add, negative to deduct).
     * Throws IllegalStateException if resulting stock would go negative.
     */
    @Transactional
    public void adjustStock(Long id, int delta, String actor, String reason) {
        Medicine medicine = findById(id);
        int newStock = medicine.getStockQuantity() + delta;
        if (newStock < 0) {
            throw new IllegalStateException(
                    "Insufficient stock for '" + medicine.getName() + "'. Current: "
                    + medicine.getStockQuantity() + ", Requested change: " + delta);
        }
        medicine.setStockQuantity(newStock);
        medicineRepository.save(medicine);
        auditLogService.log(actor, "STOCK_ADJUST",
                "Medicine: " + medicine.getName() + " (ID: " + id + "), Delta: "
                + delta + ", New stock: " + newStock + ", Reason: " + reason);
    }
}
