package com.meditrack.pharmacy.service;

import com.meditrack.pharmacy.model.AuditLog;
import com.meditrack.pharmacy.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Logs an audit entry with IP extracted from the HTTP request.
     * Uses X-Forwarded-For header (first entry) or falls back to getRemoteAddr().
     */
    public void log(String actor, String action, String details, HttpServletRequest request) {
        String ip = extractClientIp(request);
        AuditLog entry = new AuditLog(actor, action, details, ip);
        auditLogRepository.save(entry);
    }

    /**
     * Logs an audit entry for internal/system calls using "system" as the IP.
     */
    public void log(String actor, String action, String details) {
        AuditLog entry = new AuditLog(actor, action, details, "system");
        auditLogRepository.save(entry);
    }

    /**
     * Returns the most recent N audit log entries ordered by timestamp descending.
     */
    public List<AuditLog> recent(int limit) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
