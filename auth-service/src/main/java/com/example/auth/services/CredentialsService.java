package com.example.auth.services;

import com.example.auth.dtos.credentials.CredentialsDTO;
import com.example.auth.dtos.credentials.CredentialsDetailsDTO;
import com.example.auth.dtos.credentials.builders.CredentialsBuilder;
import com.example.auth.entities.Credentials;
import com.example.auth.handlers.model.ResourceNotFoundException;
import com.example.auth.repositories.CredentialsRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.auth.dtos.ChangePasswordRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CredentialsService {
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsService.class);

    public CredentialsService(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    // --- 1. ÎNREGISTRARE (Cu Debugging) ---
    @Transactional
    public Credentials register(String email, String rawPassword, String role) {
        // 🔥 DEBUG SUPREM: Vedem în consolă EXACT ce parolă vine
        System.out.println(">>> [DEBUG AUTH] REGISTER Request -> Email: " + email + " | Password: '" + rawPassword + "' | Role: " + role);

        String hash = passwordEncoder.encode(rawPassword);

        if (role == null || role.isEmpty()) {
            role = "CLIENT";
        }

        Credentials credentials = new Credentials(email, hash, role);
        return credentialsRepository.save(credentials);
    }

    // Suprascriere pentru compatibilitate
    @Transactional
    public Credentials register(String email, String rawPassword) {
        return register(email, rawPassword, "CLIENT");
    }

    // --- 2. LOGIN ---
    public Optional<Credentials> login(String email, String rawPassword) {
        Optional<Credentials> userOpt = credentialsRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Credentials user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return Optional.of(user);
            } else {
                LOGGER.warn("Login failed for {}: Password mismatch", email);
            }
        } else {
            LOGGER.warn("Login failed: User {} not found", email);
        }
        return Optional.empty();
    }

    public boolean verify(String email, String rawPassword) {
        return login(email, rawPassword).isPresent();
    }

    // --- 3. SCHIMBARE PAROLĂ ---
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Credentials user = credentialsRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Parola veche este incorectă!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credentialsRepository.save(user);
        LOGGER.info("Password changed successfully for user: {}", request.getEmail());
    }

    // --- 4. METODE STANDARD (GET, FIND) ---
    public List<CredentialsDTO> findCredentials() {
        List<Credentials> credentialsList = credentialsRepository.findAll();
        return credentialsList.stream()
                .map(CredentialsBuilder::toAuthDTO)
                .collect(Collectors.toList());
    }

    public CredentialsDTO findCredentialsById(UUID id) {
        Optional<Credentials> prosumerOptional = credentialsRepository.findById(id);
        if (prosumerOptional.isEmpty()) {
            LOGGER.error("Credentials with id {} was not found in db", id);
            throw new ResourceNotFoundException(Credentials.class.getSimpleName() + " with id: " + id);
        }
        return CredentialsBuilder.toAuthDTO(prosumerOptional.get());
    }

    public CredentialsDTO findCredentialsByEmail(String email) {
        Optional<Credentials> prosumerOptional = credentialsRepository.findByEmail(email);
        if (prosumerOptional.isEmpty()) {
            // Nu aruncăm eroare aici dacă doar verificăm existența, returnăm null sau lăsăm excepția dacă e flow critic
            return null;
        }
        return CredentialsBuilder.toAuthDTO(prosumerOptional.get());
    }

    public UUID insert(@Valid CredentialsDetailsDTO credentialsDetailsDTO) {
        Credentials credentials = CredentialsBuilder.toEntity(credentialsDetailsDTO);
        credentials = credentialsRepository.save(credentials);
        LOGGER.debug("Credentials with id {} inserted", credentials.getId());
        return credentials.getId();
    }

    @Transactional // <--- IMPORTANT: Necesar pentru ștergere
    public void deleteUserByEmail(String email) {
        if (credentialsRepository.existsByEmail(email)) {
            credentialsRepository.deleteByEmail(email);
            LOGGER.info("Credentials deleted for email: {}", email);
        } else {
            LOGGER.warn("Attempt to delete non-existent credentials for: {}", email);
        }
    }
}