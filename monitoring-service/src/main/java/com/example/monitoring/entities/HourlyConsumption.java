package com.example.monitoring.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class HourlyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID deviceId;

    // Timestamp-ul care marchează începutul orei (ex: 10:00, 11:00)
    private long hourTimestamp;

    // Suma consumului pe acea oră
    private double totalConsumption;

    public HourlyConsumption() {}

    public HourlyConsumption(UUID deviceId, long hourTimestamp, double totalConsumption) {
        this.deviceId = deviceId;
        this.hourTimestamp = hourTimestamp;
        this.totalConsumption = totalConsumption;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public long getHourTimestamp() { return hourTimestamp; }
    public void setHourTimestamp(long hourTimestamp) { this.hourTimestamp = hourTimestamp; }

    public double getTotalConsumption() { return totalConsumption; }
    public void setTotalConsumption(double totalConsumption) { this.totalConsumption = totalConsumption; }
}