/*package com.example.monitoring.services;

import com.example.monitoring.dtos.MeasurementDTO;
import com.example.monitoring.entities.Measurement;
import com.example.monitoring.repositories.MeasurementRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeasurementConsumer {

    @Autowired
    private MeasurementRepository measurementRepository;

    // Ascultă coada definită în RabbitMqConfig
    @RabbitListener(queues = "device_queue")
    public void consumeMessage(MeasurementDTO dto) {
        try {
            System.out.println("Message received from device: " + dto.getDevice_id());

            // Mapare DTO -> Entity
            Measurement measurement = new Measurement(
                    dto.getDevice_id(),
                    dto.getTimestamp(),
                    dto.getMeasurement_value()
            );

            // Salvare în DB
            measurementRepository.save(measurement);

            // AICI va trebui să adaugi logica de calcul orar (Requirements punctul 1)
            // checkHourlyConsumption(measurement);

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
*/

package com.example.monitoring.services;

import com.example.monitoring.dtos.MeasurementDTO;
import com.example.monitoring.entities.HourlyConsumption;
import com.example.monitoring.entities.Measurement;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import com.example.monitoring.repositories.MeasurementRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MeasurementConsumer {

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private HourlyConsumptionRepository hourlyConsumptionRepository;

    @RabbitListener(queues = "device_queue")
    @Transactional // Important pentru a asigura consistența datelor
    public void consumeMessage(MeasurementDTO dto) {
        try {
            System.out.println("Message received from device: " + dto.getDevice_id());

            // 1. Salvarea măsurătorii brute (opțional, dar recomandat pentru debug/istoric detaliat)
            Measurement measurement = new Measurement(
                    dto.getDevice_id(),
                    dto.getTimestamp(),
                    dto.getMeasurement_value()
            );
            measurementRepository.save(measurement);

            // 2. Calculul și stocarea consumului orar (CERINȚA OBLIGATORIE)
            updateHourlyConsumption(dto);

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateHourlyConsumption(MeasurementDTO dto) {
        // Calculăm timestamp-ul de început al orei (rotunjire la oră)
        // 3600000 ms = 1 oră
        long hourTimestamp = (dto.getTimestamp() / 3600000) * 3600000;

        // Căutăm dacă există deja o înregistrare pentru acest device la această oră
        Optional<HourlyConsumption> existingRecord =
                hourlyConsumptionRepository.findByDeviceIdAndHourTimestamp(dto.getDevice_id(), hourTimestamp);

        if (existingRecord.isPresent()) {
            // Dacă există, adăugăm valoarea nouă la total
            HourlyConsumption consumption = existingRecord.get();
            consumption.setTotalConsumption(consumption.getTotalConsumption() + dto.getMeasurement_value());
            hourlyConsumptionRepository.save(consumption);
            System.out.println("Updated hourly consumption for device " + dto.getDevice_id() + ": " + consumption.getTotalConsumption());
        } else {
            // Dacă nu există, creăm o înregistrare nouă
            HourlyConsumption newRecord = new HourlyConsumption(
                    dto.getDevice_id(),
                    hourTimestamp,
                    dto.getMeasurement_value()
            );
            hourlyConsumptionRepository.save(newRecord);
            System.out.println("Created new hourly entry for device " + dto.getDevice_id());
        }
    }
}