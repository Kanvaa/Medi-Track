package com.meditrack.pharmacy.controller;

import com.meditrack.pharmacy.model.SaleStatus;
import com.meditrack.pharmacy.service.MedicineService;
import com.meditrack.pharmacy.service.SaleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class DashboardController {

    private final MedicineService medicineService;
    private final SaleService saleService;

    public DashboardController(MedicineService medicineService, SaleService saleService) {
        this.medicineService = medicineService;
        this.saleService = saleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalMedicines", medicineService.findAll().size());
        model.addAttribute("totalSales", saleService.findAll().size());
        model.addAttribute("completedSales", saleService.countByStatus(SaleStatus.COMPLETED));

        // Calculate total revenue from COMPLETED sales
        BigDecimal totalRevenue = saleService.findByStatus(SaleStatus.COMPLETED).stream()
                .map(s -> s.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("lowStockMedicines", medicineService.lowStock());
        model.addAttribute("nearExpiryMedicines", medicineService.nearExpiry());
        model.addAttribute("expiredMedicines", medicineService.expired());
        model.addAttribute("recentSales", saleService.findRecent(5));

        return "dashboard";
    }
}
