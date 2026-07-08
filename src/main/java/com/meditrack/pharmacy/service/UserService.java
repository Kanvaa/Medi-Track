package com.meditrack.pharmacy.service;

import com.meditrack.pharmacy.model.User;
import com.meditrack.pharmacy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Transactional
    public User createUser(String username, String rawPassword, com.meditrack.pharmacy.model.Role role, String actor) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username '" + username + "' already exists.");
        }
        User user = new User(username, passwordEncoder.encode(rawPassword), role);
        User saved = userRepository.save(user);
        auditLogService.log(actor, "USER_CREATE",
                "Created user: " + username + " with role: " + role.name());
        return saved;
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled, String actor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setEnabled(enabled);
        userRepository.save(user);
        String action = enabled ? "USER_ENABLE" : "USER_DISABLE";
        auditLogService.log(actor, action, "User: " + user.getUsername());
    }

    /**
     * Resets the failed login attempts for a user — an admin convenience to let
     * a legitimate user skip the wait, not an "unlock".
     */
    @Transactional
    public void resetFailedAttempts(Long userId, String actor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setFailedLoginAttempts(0);
        user.setLastFailedAttemptAt(null);
        userRepository.save(user);
        auditLogService.log(actor, "USER_BACKOFF_RESET",
                "Reset backoff for user: " + user.getUsername());
    }

    public long count() {
        return userRepository.count();
    }
}
