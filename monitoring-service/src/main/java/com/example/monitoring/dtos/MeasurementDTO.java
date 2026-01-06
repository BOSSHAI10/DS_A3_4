package com.example.monitoring.dtos;

import java.util.UUID;

public class MeasurementDTO {
    private long timestamp;
    private UUID device_id; // Numele trebuie să coincidă cu ce trimite simulatorul sau folosești @JsonProperty
    private double measurement_value;

    // Getters and Setters
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public UUID getDevice_id() { return device_id; }
    public void setDevice_id(UUID device_id) { this.device_id = device_id; }

    public double getMeasurement_value() { return measurement_value; }
    public void setMeasurement_value(double measurement_value) { this.measurement_value = measurement_value; }
}