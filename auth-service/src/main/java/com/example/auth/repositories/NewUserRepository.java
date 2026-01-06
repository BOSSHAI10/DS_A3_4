package com.example.auth.repositories;

import com.example.auth.entities.NewUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NewUserRepository extends JpaRepository<NewUser, UUID> {

    List<NewUser> findByName(String name);

    @Query(value = "SELECT p " +
            "FROM new_users p " +
            "WHERE p.name = :name " +
            "AND p.age >= 60  ")
    Optional<List<NewUser>> findSeniorsByName(@Param("name") String name);

    @Query(value = "SELECT p " +
            "FROM new_users p " +
            "WHERE p.email = :email ")
    Optional<NewUser> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsById(UUID user_id);


}