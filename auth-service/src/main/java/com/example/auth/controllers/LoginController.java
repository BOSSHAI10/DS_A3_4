package com.example.auth.controllers;

import com.example.auth.dtos.credentials.CredentialsDetailsDTO;
import com.example.auth.entities.Credentials;
import com.example.auth.services.CredentialsService;
import com.example.auth.services.JwtService; // Asigură-te că ai acest import
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Validated
public class LoginController {

    private final CredentialsService credentialsService;
    private final JwtService jwtService;

    // --- CONSTRUCTORUL CORECTAT ---
    // Trebuie să primească ȘI JwtService ca parametru și să-l atribuie la "this.jwtService"
    public LoginController(CredentialsService credentialsService, JwtService jwtService) {
        this.credentialsService = credentialsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody CredentialsDetailsDTO credentialsDetailsDTO) {
        Optional<Credentials> userOpt = credentialsService.login(credentialsDetailsDTO.getEmail(), credentialsDetailsDTO.getPassword());

        if (userOpt.isPresent()) {
            Credentials user = userOpt.get();

            // Folosim serviciul pentru a genera token-ul
            String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("role", user.getRole());
            response.put("userId", user.getId());
            response.put("token", token);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Credențiale invalide");
        }
    }
}