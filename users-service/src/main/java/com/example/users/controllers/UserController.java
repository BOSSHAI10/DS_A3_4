package com.example.users.controllers;

import com.example.users.dtos.UserDTO;
import com.example.users.dtos.UserDetailsDTO;
import com.example.users.dtos.UserDetailsPatchDTO;
import com.example.users.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.security.Principal;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/people")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- 1. GET ALL USERS ---
    @GetMapping
    public ResponseEntity<List<UserDTO>> getPeople() {
        return ResponseEntity.ok(userService.findUsers());
    }

    // --- 2. GET USER BY ID ---
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }
    @GetMapping("/me")
    public ResponseEntity<UserDetailsDTO> getMyProfile(Principal principal) {
        // principal.getName() va returna "userId"-ul setat în IdentityFilter
        UUID myId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(userService.findUserById(myId));
    }
    // --- 3. GET USER BY EMAIL (Endpoint-ul necesar pentru Client Dashboard) ---
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDetailsDTO> getUserByEmail(@PathVariable String email) {
        // Acest apel returnează UserDetailsDTO care conține și câmpul 'name'
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    // --- 4. CREATE USER (ADMIN ONLY) ---
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> create(@Valid @RequestBody UserDetailsDTO user) {
        UUID id = userService.insert(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    // --- 5. RESTORE ---
    @PostMapping("/restore")
    public ResponseEntity<String> restore(@RequestBody List<UserDetailsDTO> userList) {
        try {
            userService.restoreUsers(userList);
            return ResponseEntity.ok("Users restored successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error restoring users: " + e.getMessage());
        }
    }

    // --- 6. UPDATE USER (ADMIN ONLY) ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDetailsDTO> update(@PathVariable UUID id, @Valid @RequestBody UserDetailsDTO user) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            return ResponseEntity.ok(userService.updateFully(id, user));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // --- 7. PARTIAL UPDATE (PATCH) ---
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> partialUpdate(@PathVariable UUID id, @RequestBody UserDetailsPatchDTO patchDto) {
        if (!userService.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        UserDetailsDTO existing = userService.findUserById(id);

        if (patchDto.getName() != null) existing.setName(patchDto.getName());
        if (patchDto.getEmail() != null) existing.setEmail(patchDto.getEmail());
        if (patchDto.getAge() != null) existing.setAge(patchDto.getAge());
        if (patchDto.getRole() != null) existing.setRole(patchDto.getRole());

        userService.updateFully(id, existing);
        return ResponseEntity.noContent().build();
    }

    // --- 8. DELETE USER (ADMIN ONLY) ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            userService.remove(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}