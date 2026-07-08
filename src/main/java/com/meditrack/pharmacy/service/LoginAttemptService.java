package com.meditrack.pharmacy.service;

import com.meditrack.pharmacy.model.User;
import com.meditrack.pharmacy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Manages login attempt tracking and exponential backoff.
 * Backoff formula: min(2^(attempts-1), 300) seconds — caps at 5 minutes.
 */
@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public LoginAttemptService(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * Returns the remaining backoff seconds for the given username.
     * Returns 0 if no backoff is active.
     */
    public long remainingBackoffSeconds(String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) {
            return 0; // Unknown user — no backoff (don't reveal user existence)
        }
        User user = optUser.get();
        if (user.getFailedLoginAttempts() == 0 || user.getLastFailedAttemptAt() == null) {
            return 0;
        }
        long requiredWait = Math.min((long) Math.pow(2, user.getFailedLoginAttempts() - 1), 300);
        long elapsed = Duration.between(user.getLastFailedAttemptAt(), LocalDateTime.now()).getSeconds();
        return Math.max(requiredWait - elapsed, 0);
    }

    /**
     * Called on successful login: resets failure counters and updates lastLoginAt.
     */
    @Transactional
    public void onSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastFailedAttemptAt(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        auditLogService.log(user.getUsername(), "LOGIN_SUCCESS", "User logged in successfully");
    }

    /**
     * Called on failed login: increments failure counter and records timestamp.
     */
    @Transactional
    public void onFailedLogin(String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            user.setLastFailedAttemptAt(LocalDateTime.now());
            userRepository.save(user);
            long nextWait = Math.min((long) Math.pow(2, user.getFailedLoginAttempts() - 1), 300);
            auditLogService.log(username, "LOGIN_FAILURE",
                    "Failed attempt #" + user.getFailedLoginAttempts() + ". Next required wait: " + nextWait + "s");
        } else {
            auditLogService.log(username, "LOGIN_FAILURE_UNKNOWN_USER",
                    "Login attempt for non-existent username");
        }
    }

    /**
     * Called when a login is rejected due to active backoff.
     * Does NOT increment the failure counter.
     */
    public void onBackoffRejected(String username, long waitSecondsRemaining) {
        auditLogService.log(username, "LOGIN_BACKOFF_REJECTED",
                "Login rejected — backoff active. " + waitSecondsRemaining + "s remaining");
    }
}
