package com.moetawol.book.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration // Marks this class as a source of bean definitions
@EnableWebSecurity // Enables Spring Security for web applications
@RequiredArgsConstructor // Automatically injects required final fields (constructor injection)
@EnableMethodSecurity(securedEnabled = true) // Enables method-level security using @Secured
public class SecurityConfig {

    // Custom filter for JWT validation
    private final JwtFilter jwtAuthFilter;

    // Custom authentication provider (e.g., DAO authentication or custom logic)
    private final AuthenticationProvider authenticationProvider;

    // Define the security filter chain bean to control security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Enable CORS with default configuration
                .cors(withDefaults())

                // 2. Disable CSRF protection (not needed in stateless JWT authentication)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Define URL patterns that are publicly accessible
                .authorizeHttpRequests(req ->
                        req.requestMatchers(
                                        "/auth/**",                   // Public auth endpoints (login, register)
                                        "/v2/api-docs",               // Swagger/OpenAPI docs
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui/**",
                                        "/webjars/**",
                                        "/swagger-ui.html"
                                ).permitAll() // Allow access without authentication
                                .anyRequest()
                                .authenticated() // All other endpoints require authentication
                )

                // 4. Make the session stateless (required for JWT usage)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )

                // 5. Use the custom authentication provider (e.g., user details + password verification)
                .authenticationProvider(authenticationProvider)

                // 6. Add the JWT filter before Spring Security's default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//                Optional configuration for OAuth2 with Keycloak (commented out):
//                .oauth2ResourceServer(auth ->
//                        auth.jwt(token -> token.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));

        // 7. Build and return the configured HttpSecurity
        return http.build();
    }
}
