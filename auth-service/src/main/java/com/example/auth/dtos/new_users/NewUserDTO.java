package com.example.auth.dtos.new_users;

import java.util.Objects;
import java.util.UUID;

public class NewUserDTO {
    private UUID id;
    private String name;
    private String email;
    private int age;

    public NewUserDTO() {}

    public NewUserDTO(UUID id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewUserDTO that = (NewUserDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(age, that.age);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name, email, age); }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
