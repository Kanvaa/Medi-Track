package com.meditrack.pharmacy.controller;

import com.meditrack.pharmacy.model.*;
import com.meditrack.pharmacy.service.MedicineService;
import com.meditrack.pharmacy.service.SaleService;
import com.meditrack.pharmacy.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final MedicineService medicineService;
    private final UserService userService;

    public SaleController(SaleService saleService,
                          MedicineService medicineService,
                          UserService userService) {
        this.saleService = saleService;
        this.medicineService = medicineService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sales", saleService.findAll());
        return "sales/list";
    }

    @GetMapping("/new")
    public String newSale(Model model) {
        Sale sale = new Sale();
        // Initialize 5 empty item rows
        List<SaleItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SaleItem item = new SaleItem();
            item.setMedicine(new Medicine());
            items.add(item);
        }
        sale.setItems(items);

        model.addAttribute("sale", sale);
        model.addAttribute("medicines", medicineService.findAll());
        model.addAttribute("statuses", SaleStatus.values());
        return "sales/form";
    }

    @PostMapping("/save")
    public String saveSale(@ModelAttribute Sale sale,
                           Authentication auth,
                           Model model,
                           RedirectAttributes redirect) {
        // Strip out item rows where no medicine was selected or quantity is 0
        List<SaleItem> validItems = new ArrayList<>();
        if (sale.getItems() != null) {
            for (SaleItem item : sale.getItems()) {
                if (item.getMedicine() != null
                        && item.getMedicine().getId() != null
                        && item.getQuantity() > 0) {
                    validItems.add(item);
                }
            }
        }

        if (validItems.isEmpty()) {
            model.addAttribute("error", "Please add at least one item to the sale.");
            model.addAttribute("sale", sale);
            model.addAttribute("medicines", medicineService.findAll());
            return "sales/form";
        }

        sale.setItems(validItems);

        try {
            User currentUser = userService.findByUsername(auth.getName());
            saleService.createSale(sale, currentUser);
            redirect.addFlashAttribute("success", "Sale completed successfully.");
            return "redirect:/sales";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            // Re-initialize empty items for the form
            while (sale.getItems().size() < 5) {
                SaleItem emptyItem = new SaleItem();
                emptyItem.setMedicine(new Medicine());
                sale.getItems().add(emptyItem);
            }
            model.addAttribute("sale", sale);
            model.addAttribute("medicines", medicineService.findAll());
            return "sales/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Sale sale = saleService.findById(id);
        model.addAttribute("sale", sale);
        model.addAttribute("statuses", SaleStatus.values());
        model.addAttribute("decryptedPhone", sale.getCustomerPhone());
        model.addAttribute("decryptedPrescription", sale.getPrescriptionNumber());
        return "sales/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam SaleStatus status,
                               Authentication auth,
                               RedirectAttributes redirect) {
        saleService.updateStatus(id, status, auth.getName());
        redirect.addFlashAttribute("success", "Sale status updated successfully.");
        return "redirect:/sales/" + id;
    }
}
