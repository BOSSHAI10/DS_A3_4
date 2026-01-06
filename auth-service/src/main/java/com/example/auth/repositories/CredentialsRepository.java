package com.example.auth.repositories;

import com.example.auth.entities.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CredentialsRepository extends JpaRepository<Credentials, UUID> {

    @Query(value = "SELECT p " +
            "FROM credentials p " +
            "WHERE p.email = :email ")
    Optional<Credentials> findByEmail(String email);
    // Optional<Auth> findByEmailAndPassword(String username, String password);
    boolean existsByEmail(String email);

    void deleteByEmail(String email);
}
