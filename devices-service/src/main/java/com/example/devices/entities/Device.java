package com.example.devices.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import java.io.Serializable;
import java.util.UUID;

@Entity(name = "devices")
@Table(name = "devices")
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "device_id", nullable = false)
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "consumption", nullable = false)
    private Integer consumption;

    @Column(name = "active", nullable = false)
    private boolean active;

    // --- MODIFICARE: Folosim 'username' (email) în loc de 'user_id' ---
    @Column(name = "username")
    private String username;

    public Device() {}

    public Device(String name, Integer consumption, boolean active) {
        this.name = name;
        this.consumption = consumption;
        this.active = active;
    }

    // Getters și Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getMaxConsumption() { return consumption; }
    public void setMaxConsumption(Integer consumption) { this.consumption = consumption; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // --- Getters/Setters Noi pentru Username ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}