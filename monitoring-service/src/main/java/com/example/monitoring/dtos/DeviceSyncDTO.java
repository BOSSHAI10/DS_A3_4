package com.example.monitoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceSyncDTO {
    private UUID deviceId;
    private String username;
    private Integer maxHourlyConsumption; // Opțional, dacă monitoring validează asta

    // 3. GETTERS - ESENȚIALI PENTRU SERIALIZARE (altfel se trimite JSON gol)
    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getMaxHourlyConsumption() {
        return maxHourlyConsumption;
    }

    public void setMaxHourlyConsumption(Integer maxHourlyConsumption) {
        this.maxHourlyConsumption = maxHourlyConsumption;
    }
}