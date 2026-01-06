package com.example.auth.controllers;

import com.example.auth.entities.Credentials;
import com.example.auth.repositories.CredentialsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class CredentialsController {

    private final CredentialsRepository credentialsRepository;

    public CredentialsController(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Credentials>> getAllCredentials() {
        return ResponseEntity.ok(credentialsRepository.findAll());
    }

    // --- METODA MODIFICATĂ: REZISTENTĂ LA DUPLICATE ---
    @PostMapping("/restore")
    public ResponseEntity<String> restoreCredentials(@RequestBody List<Credentials> credentialsList) {
        int restoredCount = 0;
        int skippedCount = 0;

        for (Credentials c : credentialsList) {
            try {
                // Verificăm dacă email-ul există deja
                if (!credentialsRepository.existsByEmail(c.getEmail())) {
                    // Dacă NU există, îl salvăm (păstrând hash-ul și ID-ul din backup)
                    credentialsRepository.save(c);
                    restoredCount++;
                } else {
                    // Dacă există, îl sărim
                    skippedCount++;
                }
            } catch (Exception e) {
                // Prindem orice altă eroare ca să nu oprim procesul
                System.err.println("Eroare la importul userului: " + c.getEmail());
            }
        }

        return ResponseEntity.ok("Restaurare completă. Adăugați: " + restoredCount + ", Ignorați (deja existenți): " + skippedCount);
    }
}