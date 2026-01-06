package com.example.users.dtos;

import com.example.users.dtos.validators.annotation.AgeLimit;
import com.example.users.entities.roles.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class UserDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email is required")
    @Email (message = "email is invalid")
    private String email;

    @NotNull(message = "age is required")
    @AgeLimit(value = 18)
    private Integer age;    // <-- schimbat din int în Integer

    @NotNull(message = "role is required")
    private Role role;

    public UserDetailsDTO() {}

    public UserDetailsDTO(String name, String email, Integer age, Role role) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.role = role;
    }

    public UserDetailsDTO(UUID id, String name, String email, Integer age, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.role = role;
    }

    // getters / setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsDTO that = (UserDetailsDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(age, that.age) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, age, role);
    }
}
