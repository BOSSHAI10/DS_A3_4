
package com.example.users.dtos;

import com.example.users.entities.roles.Role;

// Fără adnotări @NotBlank/@NotNull aici.
// DTO-ul e parțial: câmpurile nenule din request suprascriu valorile existente.
public class UserDetailsPatchDTO {
    private String name;
    private String email;
    private Integer age; // wrapper -> poate fi null
    private Role role;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
