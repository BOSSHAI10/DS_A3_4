package com.example.monitoring.repositories;

import com.example.monitoring.entities.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {
    // Poți adăuga metode custom, ex: findByDeviceIdAndTimestampBetween(...)
}