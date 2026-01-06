package com.example.api_gateway.services;


import com.example.api_gateway.clients.AuthServiceClient;
import com.example.api_gateway.clients.UsersServiceClient;
import com.example.api_gateway.dtos.RegisterRequest;
import com.example.api_gateway.idempotency.IdempotencyStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegisterOrchestrator {

    private final AuthServiceClient auth;
    private final UsersServiceClient users;
    private final IdempotencyStore idem;

    public RegisterOrchestrator(AuthServiceClient auth, UsersServiceClient users, IdempotencyStore idem) {
        this.auth = auth; this.users = users; this.idem = idem;
    }

    public Mono<Integer> register(RegisterRequest req, String idempotencyKey) {
        String role = "USER"; // force USER for public registration
        if (req.role() != null && "ADMIN".equals(req.role())) role = "USER";

        if (idempotencyKey != null && idem.seen(idempotencyKey)) {
            Integer code = idem.get(idempotencyKey);
            return Mono.just(code == null ? 200 : code);
        }

        return auth.createAuth(req.email(), req.password())
                .then(users.createUser(req.email(), req.name(), req.age(), role))
                .then(Mono.fromSupplier(() -> {
                    if (idempotencyKey != null) idem.set(idempotencyKey, 201);
                    return 201;
                }))
                .onErrorResume(ex ->
                        // Compensation: delete auth if user creation failed
                        auth.deleteAuth(req.email())
                                .onErrorResume(ignore -> Mono.empty())
                                .then(Mono.error(ex))
                );
    }

    private Mono<Integer> fallback(RegisterRequest req, String idempotencyKey, Throwable ex) {
        if (idempotencyKey != null) idem.set(idempotencyKey, 502);
        return Mono.just(502);
    }
}
