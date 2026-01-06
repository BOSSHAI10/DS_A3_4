package com.example.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final InternalSecurityFilter internalSecurityFilter;

    public SecurityConfig(InternalSecurityFilter internalSecurityFilter) {
        this.internalSecurityFilter = internalSecurityFilter;
    }

    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Orice request către users-service trebuie să treacă prin filtrul intern
                        .anyRequest().authenticated()
                )
                // Adăugăm filtrul nostru simplu ÎNAINTE de filtrul standard de login
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
     */

    // ... importuri
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Critic pentru API-uri stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- MODIFICAREA AICI ---
                        // Folosește hasAuthority, NU hasRole, dacă rolul în DB e simplu "ADMIN"
                        .requestMatchers(HttpMethod.GET, "/people/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/people/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/people/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/people/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/people/**").hasAuthority("ADMIN")
                        .requestMatchers("/ws/**").permitAll() // Permite handshake-ul WebSocket
                        // Permite accesul intern/public dacă e necesar sau alte reguli
                        .anyRequest().authenticated()
                )
                // Asigură-te că filtrul tău intern rulează ÎNAINTEA filtrului de autentificare standard
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}