package com.example.devices.controllers;

import com.example.devices.dtos.DeviceDTO;
import com.example.devices.dtos.DeviceDetailsDTO;
import com.example.devices.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody DeviceDetailsDTO deviceDetailsDTO) {
        UUID id = deviceService.insert(deviceDetailsDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    // --- MODIFICARE 1: Atribuire prin EMAIL (String), nu ID ---
    @PostMapping("/{id}/assign/{username}")
    public ResponseEntity<Void> assignDevice(@PathVariable UUID id, @PathVariable String username) {
        deviceService.assignUser(id, username); // Metoda trebuie actualizată și în Service!
        return ResponseEntity.ok().build();
    }

    // --- MODIFICARE 2: Căutare prin EMAIL (String), nu ID ---
    @GetMapping("/user/{username}")
    public ResponseEntity<List<DeviceDTO>> getDevicesByUser(@PathVariable String username) {
        return ResponseEntity.ok(deviceService.findDevicesByUsername(username)); // Metoda trebuie actualizată și în Service!
    }

    @PostMapping("/{id}/unassign")
    public ResponseEntity<Void> unassignDevice(@PathVariable UUID id) {
        deviceService.unassignUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> update(@PathVariable UUID id, @Valid @RequestBody DeviceDetailsDTO dto) {
        return ResponseEntity.ok(deviceService.update(id, dto));
    }
}