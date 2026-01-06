/*package com.example.devices.config;

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
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
     */
    // ... importuri
    /*@Bean
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
                        // Permite accesul intern/public dacă e necesar sau alte reguli
                        .anyRequest().authenticated()
                )
                // Asigură-te că filtrul tău intern rulează ÎNAINTEA filtrului de autentificare standard
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}*/

/*
package com.example.devices.config;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- FIX: Rute corecte pentru DEVICES ---
                        // Adminul poate face orice modificare pe devices
                        .requestMatchers(HttpMethod.POST, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/devices/**").hasAuthority("ADMIN")

                        // Lista generală de device-uri - de obicei doar ADMIN ar trebui să vadă tot
                        .requestMatchers(HttpMethod.GET, "/devices").hasAuthority("ADMIN")

                        // Permite utilizatorilor să își vadă propriile device-uri (endpoint-ul din controller: getDevicesByUser)
                        .requestMatchers(HttpMethod.GET, "/devices/user/**").authenticated()

                        // Orice altceva necesită autentificare
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} */

/*
package com.example.devices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final InternalSecurityFilter internalSecurityFilter;

    public SecurityConfig(InternalSecurityFilter internalSecurityFilter) {
        this.internalSecurityFilter = internalSecurityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Permitem accesul ADMIN la rutele de devices
                        .requestMatchers(HttpMethod.GET, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/devices/**").hasAuthority("ADMIN")

                        // 2. Permitem utilizatorilor să își vadă propriile device-uri (dacă ai acest endpoint)
                        .requestMatchers("/devices/user/**").authenticated()

                        // Orice altceva
                        .anyRequest().authenticated()
                )
                // --- FIXUL ESTE AICI: Configurăm conversia JWT ---
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                // Păstrăm și filtrul intern pentru comunicare între servicii (dacă e folosit)
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Această metodă îi spune lui Spring: "Ia rolurile din claim-ul 'role' și nu le pune prefix"
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Elimină prefixul default "SCOPE_" sau "ROLE_"
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role"); // Citește din cheia "role" (definită în JwtService)

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}*/
/*
package com.example.devices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final InternalSecurityFilter internalSecurityFilter;

    @Value("${jwt.secret}") // Citim secretul din application.properties
    private String jwtSecret;

    public SecurityConfig(InternalSecurityFilter internalSecurityFilter) {
        this.internalSecurityFilter = internalSecurityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers("/devices/user/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder()) // Specificăm manual decoder-ul
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- BEAN-UL CARE LIPSEA ---
    @Bean
    public JwtDecoder jwtDecoder() {
        // Trebuie să decodăm cheia dacă e Base64 (cum e de obicei în JwtUtil)
        byte[] secretBytes = Base64.getDecoder().decode(jwtSecret);
        SecretKeySpec secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}*/

package com.example.devices.config;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitem accesul ADMIN la rutele de modificare devices
                        .requestMatchers(HttpMethod.POST, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/devices/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/devices/**").hasAuthority("ADMIN")

                        // Lista completă de device-uri doar pentru ADMIN
                        .requestMatchers(HttpMethod.GET, "/devices").hasAuthority("ADMIN")

                        // Permitem utilizatorilor să își vadă propriile device-uri (endpoint specific)
                        .requestMatchers("/devices/user/**").authenticated()

                        // Orice altceva necesită autentificare
                        .anyRequest().authenticated()
                )
                // ELIMINĂM oauth2ResourceServer - ne bazăm pe filtrul intern
                .addFilterBefore(internalSecurityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}