package com.example.devices.dtos;

import java.util.UUID;

public class DeviceDetailsDTO {
    private UUID id;
    private String name;
    private Integer consumption;
    private boolean active;
    private String username; // Adăugat pentru consistență

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(UUID id, String name, Integer consumption, boolean active, String username) {
        this.id = id;
        this.name = name;
        this.consumption = consumption;
        this.active = active;
        this.username = username;
    }

    public DeviceDetailsDTO(String name, Integer consumption, boolean active) {
        this.name = name;
        this.consumption = consumption;
        this.active = active;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getConsumption() { return consumption; }
    public void setConsumption(Integer consumption) { this.consumption = consumption; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}