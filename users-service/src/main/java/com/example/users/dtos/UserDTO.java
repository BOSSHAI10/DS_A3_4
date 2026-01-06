package com.example.users.dtos;

import com.example.users.entities.roles.Role;
import java.util.Objects;
import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private int age;
    private Role role; // Păstrăm DOAR acest câmp

    public UserDTO() {
    }

    public UserDTO(UUID id, String name, String email, int age, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    // --- Getters și Setters pentru Role (Unici și Corecți) ---
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return age == userDTO.age &&
                Objects.equals(id, userDTO.id) &&
                Objects.equals(name, userDTO.name) &&
                Objects.equals(email, userDTO.email) &&
                role == userDTO.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, age, role);
    }
}