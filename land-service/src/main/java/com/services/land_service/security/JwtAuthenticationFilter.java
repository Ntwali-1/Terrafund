package com.services.land_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);

            if (jwtService.validateToken(jwt)) {
                String userEmail = jwtService.extractEmail(jwt);
                Long userId = jwtService.extractUserId(jwt);
                List<String> roles = jwtService.extractRoles(jwt);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Convert roles to GrantedAuthority
                    List<SimpleGrantedAuthority> authorities = roles != null ?
                            roles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList())
                            : List.of();

                    // Create custom authentication object with userId
                    UserAuthenticationToken authToken = new UserAuthenticationToken(
                            userEmail,
                            userId,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } else {
                // Token is invalid, but don't fail - let Spring Security decide based on endpoint config
                logger.warn("Invalid JWT token, continuing without authentication");
            }
        } catch (Exception e) {
            // Token validation failed, but don't fail the request - let Spring Security handle it
            logger.error("JWT validation error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}