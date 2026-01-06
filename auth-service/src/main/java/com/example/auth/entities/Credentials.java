package com.example.auth.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Entity(name = "credentials")
@Table(name = "credentials",
        indexes = {
                @Index(name = "idx_credentials_email", columnList = "email", unique = true)
        })
public class Credentials implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "credential_id", nullable = false)
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @NotBlank
    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;

    // --- MODIFICARE: Adăugat câmpul ROLE ---
    @Column(name = "role", nullable = false)
    private String role = "USER"; // Rolul implicit este CLIENT

    public Credentials() {
    }

    // Constructor actualizat să includă și rolul
    public Credentials(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- MODIFICARE: Getteri și Setteri pentru Role ---
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}