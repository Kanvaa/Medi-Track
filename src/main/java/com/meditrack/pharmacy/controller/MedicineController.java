package com.meditrack.pharmacy.controller;

import com.meditrack.pharmacy.model.Medicine;
import com.meditrack.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("medicines", medicineService.findAll());
        return "medicines/list";
    }

    @GetMapping("/new")
    public String newMedicine(Model model) {
        model.addAttribute("medicine", new Medicine());
        return "medicines/form";
    }

    @GetMapping("/{id}/edit")
    public String editMedicine(@PathVariable Long id, Model model) {
        model.addAttribute("medicine", medicineService.findById(id));
        return "medicines/form";
    }

    @PostMapping("/save")
    public String saveMedicine(@Valid @ModelAttribute Medicine medicine,
                               BindingResult result,
                               Authentication auth,
                               RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "medicines/form";
        }
        medicineService.save(medicine, auth.getName());
        redirect.addFlashAttribute("success", "Medicine saved successfully.");
        return "redirect:/medicines";
    }

    @PostMapping("/{id}/delete")
    public String deleteMedicine(@PathVariable Long id,
                                 Authentication auth,
                                 RedirectAttributes redirect) {
        medicineService.delete(id, auth.getName());
        redirect.addFlashAttribute("success", "Medicine deleted successfully.");
        return "redirect:/medicines";
    }
}
