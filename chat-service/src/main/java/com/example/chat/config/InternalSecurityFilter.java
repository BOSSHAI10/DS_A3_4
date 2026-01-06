package com.example.chat.config;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Verificăm cheia secretă
        String requestSecret = request.getHeader("X-Internal-Secret");

        if (requestSecret == null || !requestSecret.equals(internalSecret)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acces interzis: Request-ul nu vine din Gateway.");
            return;
        }

        // 2. Extragem identitatea (garantată de Gateway)
        String username = request.getHeader("X-Authenticated-User");
        String role = request.getHeader("X-User-Role");

        if (username != null) {
            // Reconstruim user-ul în contextul Spring Security
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(authority));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}