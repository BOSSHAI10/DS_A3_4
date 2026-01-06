package com.example.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.time.Instant;
import java.util.LinkedHashMap;

public class CounterSimulator {

    private static final String QUEUE_NAME = "device_queue";

    private static final String CONFIG_FILE = "sensor_config.properties";

    public static void main(String[] args) {
        System.out.println(">>> CounterSimulator: Starting...");

        String deviceId = readDeviceId();
        if (deviceId == null) {
            System.err.println("Eroare: Nu s-a putut citi device_id din " + CONFIG_FILE);
            return;
        }
        System.out.println(">>> Simulation for Device ID: " + deviceId);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            ObjectMapper mapper = new ObjectMapper();
            Random random = new Random();

            while (true) {
                double currentConsumption = generateRealisticConsumption(random);
                long timestamp = System.currentTimeMillis();
                //String timestamp = Instant.now().toString();

                Map<String, Object> messageData = new LinkedHashMap<>();
                messageData.put("timestamp", timestamp);
                messageData.put("device_id", deviceId);
                messageData.put("measurement_value", currentConsumption);

                String jsonMessage = mapper.writeValueAsString(messageData);

                channel.basicPublish("", QUEUE_NAME, null, jsonMessage.getBytes());
                System.out.println(" [x] Sent: " + jsonMessage);

                TimeUnit.SECONDS.sleep(3); // Timp redus pentru probe
                //TimeUnit.MINUTES.sleep(10);
            }

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String readDeviceId() {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            prop.load(input);
            return prop.getProperty("device_id");
        } catch (IOException ex) {
            // Încercăm să citim din resources dacă nu e găsit în root (util dacă rulezi din IDE vs JAR)
            try {
                prop.load(CounterSimulator.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
                return prop.getProperty("device_id");
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static double generateRealisticConsumption(Random random) {
        int hour = LocalTime.now().getHour();
        double baseValue;

        if (hour >= 23 || hour < 6) {
            baseValue = 0.2; // Noaptea
        } else if (hour >= 18 && hour < 23) {
            baseValue = 2.5; // Seara
        } else {
            baseValue = 1.0; // Ziua
        }

        double fluctuation = (random.nextDouble() - 0.5);
        return Math.max(0.1, Math.round((baseValue + fluctuation) * 100.0) / 100.0);
    }
}