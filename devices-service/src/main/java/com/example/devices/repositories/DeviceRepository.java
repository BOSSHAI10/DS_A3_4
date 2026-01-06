package com.example.devices.repositories;

import com.example.devices.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Device findByName(String name);

    @Query(value = "SELECT p FROM devices p WHERE p.consumption > 100")
    Optional<Device> findBigConsumers(@Param("consumption") int consumption);

    // --- MODIFICARE: Căutare după username ---
    List<Device> findByUsername(String username);
}