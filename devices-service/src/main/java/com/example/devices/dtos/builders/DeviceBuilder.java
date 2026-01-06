package com.example.devices.dtos.builders;

import com.example.devices.dtos.DeviceDTO;
import com.example.devices.dtos.DeviceDetailsDTO;
import com.example.devices.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.isActive(),
                device.getUsername()
        );
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getMaxConsumption(),
                device.isActive(),
                device.getUsername()
        );
    }

    public static Device toEntity(DeviceDetailsDTO deviceDetailsDTO) {
        // 1. Creăm entitatea cu datele de bază
        Device device = new Device(
                deviceDetailsDTO.getName(),
                deviceDetailsDTO.getConsumption(),
                deviceDetailsDTO.isActive()
        );

        // 2. --- MODIFICARE CRITICĂ PENTRU RESTORE ---
        // Setăm și username-ul (email-ul proprietarului) dacă există în DTO
        device.setUsername(deviceDetailsDTO.getUsername());

        return device;
    }
}