package com.meditrack.pharmacy.controller;

import com.meditrack.pharmacy.model.Role;
import com.meditrack.pharmacy.service.AuditLogService;
import com.meditrack.pharmacy.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/admin/users/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam Role role,
                             Authentication auth,
                             RedirectAttributes redirect) {
        try {
            userService.createUser(username, password, role, auth.getName());
            redirect.addFlashAttribute("success", "User '" + username + "' created successfully.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id,
                             @RequestParam boolean enabled,
                             Authentication auth,
                             RedirectAttributes redirect) {
        userService.setEnabled(id, enabled, auth.getName());
        redirect.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/reset-attempts")
    public String resetAttempts(@PathVariable Long id,
                                Authentication auth,
                                RedirectAttributes redirect) {
        userService.resetFailedAttempts(id, auth.getName());
        redirect.addFlashAttribute("success", "Login backoff reset successfully.");
        return "redirect:/admin/users";
    }

    @GetMapping("/audit-log")
    public String auditLog(Model model) {
        model.addAttribute("logs", auditLogService.recent(200));
        return "admin/audit-log";
    }
}
