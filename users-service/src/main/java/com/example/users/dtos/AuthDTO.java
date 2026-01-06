package com.example.users.dtos;

public class AuthDTO {
    private String email;
    private String password;
    private String role;

    public AuthDTO(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters sunt obligatorii pentru ca RestTemplate să poată face JSON-ul
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}