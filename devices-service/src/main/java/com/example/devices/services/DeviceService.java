package com.example.devices.services;

import com.example.devices.dtos.DeviceDTO;
import com.example.devices.dtos.DeviceDetailsDTO;
import com.example.devices.dtos.DeviceSyncDTO;
import com.example.devices.dtos.builders.DeviceBuilder;
import com.example.devices.entities.Device;
import com.example.devices.repositories.DeviceRepository;
import com.example.devices.handlers.exceptions.model.ResourceNotFoundException; // Asigură-te că importul e corect pentru proiectul tău
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, RabbitTemplate rabbitTemplate) {
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<DeviceDTO> findDevices() {
        return deviceRepository.findAll().stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> device = deviceRepository.findById(id);
        if (device.isEmpty()) {
            throw new ResourceNotFoundException("Device not found: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(device.get());
    }

    @Transactional
    public UUID insert(DeviceDetailsDTO deviceDetailsDTO) {
        Device device = DeviceBuilder.toEntity(deviceDetailsDTO);
        device = deviceRepository.save(device);

        // SINCRONIZARE
        DeviceSyncDTO syncData = new DeviceSyncDTO(
                device.getId(),
                device.getUsername(), // Asigură-te că ai userId în entitatea Device
                device.getMaxConsumption()
        );
        rabbitTemplate.convertAndSend("device_queue", syncData);

        return device.getId();
    }

    @Transactional
    public void assignUser(UUID deviceId, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        device.setUsername(username);
        deviceRepository.save(device);

        // SINCRONIZARE
        DeviceSyncDTO syncData = new DeviceSyncDTO(
                device.getId(),
                device.getUsername(), // Asigură-te că ai userId în entitatea Device
                device.getMaxConsumption()
        );
        rabbitTemplate.convertAndSend("device_queue", syncData);

        LOGGER.info("Device {} assigned to user {}", deviceId, username);
    }

    public List<DeviceDTO> findDevicesByUsername(String username) {
        return deviceRepository.findByUsername(username).stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void unassignUser(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        device.setUsername(null);
        deviceRepository.save(device);

        // SINCRONIZARE
        DeviceSyncDTO syncData = new DeviceSyncDTO(
                device.getId(),
                device.getUsername(), // Asigură-te că ai userId în entitatea Device
                device.getMaxConsumption()
        );
        rabbitTemplate.convertAndSend("device_queue", syncData);
    }

    @Transactional
    public void delete(UUID id) {
        deviceRepository.deleteById(id);
        rabbitTemplate.convertAndSend("device_queue", id);
    }

    public DeviceDetailsDTO update(UUID id, DeviceDetailsDTO dto) {
        Optional<Device> deviceOpt = deviceRepository.findById(id);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.setName(dto.getName());
            device.setMaxConsumption(dto.getConsumption());
            device.setActive(dto.isActive());

            // SINCRONIZARE
            DeviceSyncDTO syncData = new DeviceSyncDTO(
                    device.getId(),
                    device.getUsername(), // Asigură-te că ai userId în entitatea Device
                    device.getMaxConsumption()
            );
            rabbitTemplate.convertAndSend("device_queue", syncData);

            return DeviceBuilder.toDeviceDetailsDTO(deviceRepository.save(device));
        }
        throw new ResourceNotFoundException("Device not found");
    }
}