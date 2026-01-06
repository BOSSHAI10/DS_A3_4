package com.example.api_gateway.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {
    private final WebClient client;
    public AuthServiceClient(WebClient authClient) { this.client = authClient; }

    public Mono<Void> createAuth(String email, String password) {
        return client.post().uri("/api/auth")
                .bodyValue(new AuthPayload(email, password))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), resp -> resp.createException())
                .onStatus(s -> s.is5xxServerError(), resp -> resp.createException())
                .toBodilessEntity().then();
    }

    public Mono<Void> deleteAuth(String email) {
        return client.delete().uri("/api/auth/{email}", email)
                .retrieve()
                .toBodilessEntity().then();
    }

    record AuthPayload(String email, String password) {}
}
