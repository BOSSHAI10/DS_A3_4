package com.example.auth.dtos.credentials.builders;

import com.example.auth.dtos.credentials.CredentialsDTO;
import com.example.auth.dtos.credentials.CredentialsDetailsDTO;
import com.example.auth.entities.Credentials;

public class CredentialsBuilder {

    private CredentialsBuilder() {
    }

    public static CredentialsDetailsDTO toAuthDetailsDTO(Credentials credentials) {
        // --- MODIFICARE: Folosim constructorul cu 3 parametri (inclusiv rolul) ---
        return new CredentialsDetailsDTO(credentials.getEmail(), credentials.getPassword(), credentials.getRole());
    }

    public static Credentials toEntity(CredentialsDetailsDTO credentialsDetailsDTO) {
        // --- MODIFICARE: Adăugat logică pentru rol ---
        String role = credentialsDetailsDTO.getRole();
        if (role == null || role.isEmpty()) {
            role = "CLIENT";
        }

        return new Credentials(
                credentialsDetailsDTO.getEmail(),
                credentialsDetailsDTO.getPassword(),
                role
        );
    }

    // Entity -> Output DTO
    public static CredentialsDTO toAuthDTO(Credentials entity) {
        return new CredentialsDTO(entity.getId(), entity.getEmail());
    }
}