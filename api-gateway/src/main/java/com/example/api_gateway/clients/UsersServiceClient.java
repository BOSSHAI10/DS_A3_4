package com.example.api_gateway.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UsersServiceClient {
    private final WebClient client;
    public UsersServiceClient(WebClient usersClient) { this.client = usersClient; }

    public Mono<Void> createUser(String email, String name, int age, String role) {
        return client.post().uri("/api/users")
                .bodyValue(new UserPayload(email, name, age, role))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), resp -> resp.createException())
                .onStatus(s -> s.is5xxServerError(), resp -> resp.createException())
                .toBodilessEntity().then();
    }

    record UserPayload(String email, String name, int age, String role) {}
}
