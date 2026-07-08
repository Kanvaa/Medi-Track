package com.meditrack.pharmacy.security;

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

    public LoginFailureHandler() {
        super("/login?error=true");
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
            // Generic message — don't reveal whether username or password was wrong
            message = "Invalid username or password.";
        }

        request.getSession().setAttribute("loginMessage", message);
        super.onAuthenticationFailure(request, response, exception);
    }
}
