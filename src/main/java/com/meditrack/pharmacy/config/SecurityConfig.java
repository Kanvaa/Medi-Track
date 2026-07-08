package com.meditrack.pharmacy.config;

import com.meditrack.pharmacy.security.BackoffAwareAuthenticationProvider;
import com.meditrack.pharmacy.security.CustomUserDetailsService;
import com.meditrack.pharmacy.security.LoginFailureHandler;
import com.meditrack.pharmacy.service.LoginAttemptService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;
    private final LoginFailureHandler loginFailureHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          LoginAttemptService loginAttemptService,
                          LoginFailureHandler loginFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.loginAttemptService = loginAttemptService;
        this.loginFailureHandler = loginFailureHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(userDetailsService);
        dao.setPasswordEncoder(passwordEncoder());

        return new BackoffAwareAuthenticationProvider(dao, loginAttemptService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/webjars/**", "/login").permitAll()
                .requestMatchers("/admin/**", "/audit-log").hasRole("OWNER")
                .requestMatchers("/medicines/new", "/medicines/*/edit", "/medicines/*/delete")
                    .hasAnyRole("OWNER", "PHARMACIST")
                .requestMatchers("/sales/new", "/sales/*/status")
                    .hasAnyRole("OWNER", "PHARMACIST", "STAFF")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(loginFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().newSession()
                .maximumSessions(1)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
            );

        return http.build();
    }
}
