package com.example.api_gateway.controllers;


import com.example.api_gateway.dtos.RegisterRequest;
import com.example.api_gateway.services.RegisterOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final RegisterOrchestrator orchestrator;
    public RegisterController(RegisterOrchestrator orchestrator) { this.orchestrator = orchestrator; }

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(
            @Valid @RequestBody RegisterRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {

        if (req.age() < 18) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return orchestrator.register(req, idemKey)
                .map(code -> ResponseEntity.status(code).build())
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(409).build())); // map duplicates, adjust as needed
    }
}
