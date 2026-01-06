package com.example.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class InternalSecurityFilter extends OncePerRequestFilter {

    @Value("${internal.secret}")
    private String internalSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Excludem rutele publice de la verificarea secretului
        // Gateway-ul nu pune header-ul secret pe rutele publice, deci trebuie să le lăsăm să treacă
        String path = request.getRequestURI();
        return path.startsWith("/auth/login") || path.startsWith("/auth/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Verificăm cheia secretă pentru rutele protejate (ex: change password)
        String requestSecret = request.getHeader("X-Internal-Secret");

        if (requestSecret == null || !requestSecret.equals(internalSecret)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acces interzis: Request-ul nu vine din Gateway sau secretul e invalid.");
            return;
        }

        // 2. Extragem identitatea (dacă există)
        String username = request.getHeader("X-Authenticated-User");
        String role = request.getHeader("X-User-Role");

        if (username != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(authority));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}