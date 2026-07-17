package com.meditrack.pharmacy.security;

import com.meditrack.pharmacy.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom login failure handler that stores the appropriate error message in the session.
 * Shows the actual wait time for backoff, or a generic message for wrong credentials.
 */
@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    public LoginFailureHandler(LoginAttemptService loginAttemptService) {
        super("/login?error=true");
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String message;
        if (exception instanceof AuthenticationServiceException) {
            // Backoff message — show the actual wait time
            message = exception.getMessage();
        } else {
            // Generic message + check if backoff timer is now active for this username
            String username = request.getParameter("username");
            long wait = loginAttemptService.remainingBackoffSeconds(username);
            if (wait > 0) {
                message = "Invalid credentials. Account locked for " + wait + " seconds.";
            } else {
                message = "Invalid username or password.";
            }
        }

        request.getSession().setAttribute("loginMessage", message);
        super.onAuthenticationFailure(request, response, exception);
    }
}
