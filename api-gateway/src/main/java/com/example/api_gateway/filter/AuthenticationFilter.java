package com.example.api_gateway.filter;

import com.example.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    // Citim cheia secretă internă din application.properties
    @Value("${internal.secret}")
    private String internalSecret;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {
                // 1. Verificăm existența header-ului
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authorization header");
                }

                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // 2. Validăm token-ul
                    // Asigură-te că metoda validateToken aruncă excepție sau returnează false dacă e invalid
                    jwtUtil.validateToken(authHeader);

                    // 3. Extragem datele (Claims)
                    Claims claims = jwtUtil.getAllClaimsFromToken(authHeader); // Verifică dacă metoda se numește getClaims sau getAllClaimsFromToken în JwtUtil-ul tău

                    // 4. Mutăm request-ul (Identity Propagation)
                    // Adăugăm headere noi pe care microserviciile le vor citi
                    request = exchange.getRequest().mutate()
                            .header("X-Authenticated-User", claims.getSubject()) // Username
                            .header("X-User-Role", claims.get("role", String.class)) // Role (asigură-te că cheia în token e "role")
                            .header("X-Internal-Secret", internalSecret) // Cheia de siguranță
                            .build();

                } catch (Exception e) {
                    System.out.println("Acces invalid: " + e.getMessage());
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acces neautorizat la aplicatie");
                }
            }
            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    public static class Config {
    }
}