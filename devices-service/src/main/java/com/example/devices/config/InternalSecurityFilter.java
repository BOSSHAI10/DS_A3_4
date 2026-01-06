package com.example.devices.config;

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

        String requestSecret = request.getHeader("X-Internal-Secret");
        System.out.println("DEBUG: Secret primit: " + requestSecret);
        System.out.println("DEBUG: Secret asteptat: " + internalSecret);

        if (requestSecret == null || !requestSecret.equals(internalSecret)) {
            System.out.println("DEBUG: Secret invalid!");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: Invalid Internal Secret");
            return;
        }

        String username = request.getHeader("X-Authenticated-User");
        System.out.println("DEBUG: Username primit: " + username);
        String role = request.getHeader("X-User-Role");
        System.out.println("DEBUG: Role primit: " + role);

        if (username != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}