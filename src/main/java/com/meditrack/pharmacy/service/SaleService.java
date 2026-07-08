package com.meditrack.pharmacy.service;

import com.meditrack.pharmacy.model.*;
import com.meditrack.pharmacy.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final MedicineService medicineService;
    private final AuditLogService auditLogService;

    public SaleService(SaleRepository saleRepository,
                       MedicineService medicineService,
                       AuditLogService auditLogService) {
        this.saleRepository = saleRepository;
        this.medicineService = medicineService;
        this.auditLogService = auditLogService;
    }

    public List<Sale> findAll() {
        return saleRepository.findAllByOrderByCreatedAtDesc();
    }

    public Sale findById(Long id) {
        return saleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with id: " + id));
    }

    public List<Sale> findRecent(int limit) {
        return saleRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public long countByStatus(SaleStatus status) {
        return saleRepository.countByStatus(status);
    }

    public List<Sale> findByStatus(SaleStatus status) {
        return saleRepository.findByStatus(status);
    }

    /**
     * Creates a sale following the 8-step business rule flow.
     * The entire operation is transactional — a failure at any step rolls back everything.
     */
    @Transactional
    public Sale createSale(Sale sale, User creator) {
        boolean requiresPrescription = false;

        for (SaleItem item : sale.getItems()) {
            // Step 1: Look up the real Medicine by id
            Medicine medicine = medicineService.findById(item.getMedicine().getId());

            // Step 2: Check if expired — reject ENTIRE sale
            if (medicine.isExpired()) {
                auditLogService.log(creator.getUsername(), "EXPIRED_STOCK_BLOCKED",
                        "Attempted sale of expired medicine: " + medicine.getName()
                        + ", Batch: " + medicine.getBatchNumber()
                        + ", Expired: " + medicine.getExpiryDate());
                throw new IllegalStateException(
                        "Cannot sell expired medicine: '" + medicine.getName()
                        + "' (Batch: " + medicine.getBatchNumber()
                        + ", Expired: " + medicine.getExpiryDate() + ")");
            }

            // Step 3: Track if prescription is needed
            if (medicine.isPrescriptionRequired()) {
                requiresPrescription = true;
            }

            // Step 5: Snapshot price and batch from the medicine at moment of sale
            item.setUnitPriceAtSale(medicine.getPrice());
            item.setBatchNumberAtSale(medicine.getBatchNumber());
            item.setMedicine(medicine);
        }

        // Step 3 (continued): Validate prescription if any item requires it
        if (requiresPrescription) {
            if (sale.getPrescriptionNumber() == null || sale.getPrescriptionNumber().isBlank()) {
                throw new IllegalStateException(
                        "A prescription number is required because this sale includes prescription-required medicine(s).");
            }
        }

        // Step 4: Deduct stock for each item
        for (SaleItem item : sale.getItems()) {
            medicineService.adjustStock(
                    item.getMedicine().getId(),
                    -item.getQuantity(),
                    creator.getUsername(),
                    "Sale deduction");
        }

        // Step 6: Calculate total amount
        BigDecimal total = sale.getItems().stream()
                .map(SaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sale.setTotalAmount(total);

        // Set the seller and ensure bidirectional relationship
        sale.setSoldBy(creator);
        for (SaleItem item : sale.getItems()) {
            item.setSale(sale);
        }

        // Step 7: Save and log
        Sale saved = saleRepository.save(sale);
        auditLogService.log(creator.getUsername(), "SALE_CREATE",
                "Sale ID: " + saved.getId() + ", Customer: " + saved.getCustomerName()
                + ", Total: $" + saved.getTotalAmount());

        return saved;
    }

    /**
     * Updates sale status. Restocks items when changing from COMPLETED to CANCELLED or RETURNED.
     */
    @Transactional
    public Sale updateStatus(Long saleId, SaleStatus newStatus, String actor) {
        Sale sale = findById(saleId);
        SaleStatus oldStatus = sale.getStatus();

        // Restock if changing from COMPLETED to CANCELLED or RETURNED
        if (oldStatus == SaleStatus.COMPLETED
                && (newStatus == SaleStatus.CANCELLED || newStatus == SaleStatus.RETURNED)) {
            for (SaleItem item : sale.getItems()) {
                medicineService.adjustStock(
                        item.getMedicine().getId(),
                        item.getQuantity(),  // positive delta = restock
                        actor,
                        "Restock due to sale " + newStatus.name().toLowerCase());
            }
        }

        sale.setStatus(newStatus);
        Sale saved = saleRepository.save(sale);

        auditLogService.log(actor, "SALE_STATUS_CHANGE",
                "Sale ID: " + saleId + ", Status: " + oldStatus + " -> " + newStatus);

        return saved;
    }
}
