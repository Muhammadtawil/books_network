package com.moetawol.book.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This filter intercepts every HTTP request once and processes JWT authentication if applicable.
@Service
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // Inject the JwtService to extract and validate tokens.
    private final JwtService jwtService;

    // Inject the UserDetailsService to load user data from the DB or another source.
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. Skip authentication for endpoints related to log in /register (public endpoints).
        if (request.getServletPath().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response); // pass request along the filter chain
            return;
        }

        // 2. Get the Authorization header from the request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 3. Check if header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // no JWT found, continue without authentication
            return;
        }

        // 4. Extract JWT token from the header (remove Bearer)
        jwt = authHeader.substring(7);

        // 5. Extract username (email) from JWT token
        userEmail = jwtService.extractUsername(jwt);

        // 6. If userEmail is valid and there's no authentication set in the context yet
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 7. Load the user details from the database or configured user store
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 8. Check if the token is valid for this user
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 9. Create authentication object with user details and roles
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // 10. Attach request-specific details (like IP, session, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 11. Set the authentication in the SecurityContext (user is now authenticated)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 12. Continue processing the request
        filterChain.doFilter(request, response);
    }
}
