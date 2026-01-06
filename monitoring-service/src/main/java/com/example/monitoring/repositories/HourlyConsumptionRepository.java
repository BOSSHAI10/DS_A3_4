package com.example.monitoring.repositories;

import com.example.monitoring.entities.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, UUID> {
    // Metodă pentru a găsi înregistrarea specifică unui device și unei ore
    Optional<HourlyConsumption> findByDeviceIdAndHourTimestamp(UUID deviceId, long hourTimestamp);
}