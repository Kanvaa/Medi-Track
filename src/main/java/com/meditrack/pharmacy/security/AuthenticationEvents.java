package com.meditrack.pharmacy.security;

import com.meditrack.pharmacy.model.User;
import com.meditrack.pharmacy.repository.UserRepository;
import com.meditrack.pharmacy.service.LoginAttemptService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listens for Spring Security authentication events to update login attempt tracking.
 */
@Component
public class AuthenticationEvents {

    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;

    public AuthenticationEvents(LoginAttemptService loginAttemptService,
                                UserRepository userRepository) {
        this.loginAttemptService = loginAttemptService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        Optional<User> optUser = userRepository.findByUsername(username);
        optUser.ifPresent(loginAttemptService::onSuccessfulLogin);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        // Skip if this is a backoff rejection — already logged by the provider
        if (event.getException() instanceof AuthenticationServiceException) {
            return;
        }
        String username = event.getAuthentication().getName();
        loginAttemptService.onFailedLogin(username);
    }
}
