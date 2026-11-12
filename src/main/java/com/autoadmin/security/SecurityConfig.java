package com.autoadmin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService adminUserDetailsService(
        @Value("${ADMIN_USERNAME:}") String username,
        @Value("${ADMIN_PASSWORD:}") String password,
        PasswordEncoder passwordEncoder
    ) {
        if (username.isBlank() && password.isBlank()) {
            log.warn("Admin login is disabled until ADMIN_USERNAME and ADMIN_PASSWORD are configured.");
            return new InMemoryUserDetailsManager();
        }
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Configure both ADMIN_USERNAME and ADMIN_PASSWORD to enable the Admin Dashboard.");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least 12 characters long.");
        }
        return new InMemoryUserDetailsManager(User.withUsername(username)
            .password(passwordEncoder.encode(password))
            .roles("ADMIN")
            .build());
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository(
        @Value("${SESSION_COOKIE_SECURE:true}") boolean secureCookie,
        @Value("${SESSION_COOKIE_SAMESITE:None}") String sameSite
    ) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieCustomizer(cookie -> cookie
            .path("/")
            .secure(secureCookie)
            .sameSite(sameSite));
        return repository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CsrfTokenRepository csrfTokens) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokens)
                .csrfTokenRequestHandler(csrfHandler))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/", "/index.html", "/styles.css", "/app.js", "/login.js", "/manus-routes.json",
                    "/admin-login.html", "/images/**", "/manus-storage/**", "/favicon.ico",
                    "/services", "/contact", "/new-cars", "/used-cars", "/admin/login",
                    "/api/csrf", "/api/vehicles/**", "/api/contact", "/api/contact/**", "/api/purchases/**", "/error"
                ).permitAll()
                .requestMatchers("/api/admin/**", "/api/listings/**", "/admin", "/admin/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/admin/login?error")
                .permitAll())
            // Cloud Preview is embedded cross-site; Admin APIs still require an authenticated role and CSRF token.
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN"))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(401, "Admin authentication is required.");
                    } else {
                        response.sendRedirect("/admin/login");
                    }
                })
                .accessDeniedHandler((request, response, exception) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(403, "This action is not allowed.");
                    } else {
                        response.sendError(403);
                    }
                }));
        return http.build();
    }
}
