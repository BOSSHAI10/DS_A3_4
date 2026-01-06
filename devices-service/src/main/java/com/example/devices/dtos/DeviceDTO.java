package com.example.devices.dtos;

import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private Integer consumption;
    private boolean active;

    // --- MODIFICARE: Username în loc de UserId ---
    private String username;

    public DeviceDTO() {
    }

    public DeviceDTO(UUID id, String name, Integer consumption, boolean active, String username) {
        this.id = id;
        this.name = name;
        this.consumption = consumption;
        this.active = active;
        this.username = username;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getConsumption() {
        return consumption;
    }

    public void setConsumption(Integer consumption) {
        this.consumption = consumption;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}