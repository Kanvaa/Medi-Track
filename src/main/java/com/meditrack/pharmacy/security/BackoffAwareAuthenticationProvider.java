package com.meditrack.pharmacy.security;

import com.meditrack.pharmacy.service.LoginAttemptService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Wraps a DaoAuthenticationProvider to check for exponential backoff before
 * delegating authentication. If backoff is active, throws AuthenticationServiceException
 * WITHOUT checking the password at all.
 */
public class BackoffAwareAuthenticationProvider implements AuthenticationProvider {

    private final DaoAuthenticationProvider delegate;
    private final LoginAttemptService loginAttemptService;

    public BackoffAwareAuthenticationProvider(DaoAuthenticationProvider delegate,
                                              LoginAttemptService loginAttemptService) {
        this.delegate = delegate;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        long wait = loginAttemptService.remainingBackoffSeconds(username);
        if (wait > 0) {
            loginAttemptService.onBackoffRejected(username, wait);
            throw new AuthenticationServiceException(
                    "Too many failed attempts. Please wait " + wait + " second(s) before trying again.");
        }
        return delegate.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
}
