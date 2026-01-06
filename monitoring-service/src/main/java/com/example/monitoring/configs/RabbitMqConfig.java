package com.example.monitoring.configs;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    // Definim numele cozilor exact cum le-am folosit în Users și Devices service
    public static final String USER_SYNC_QUEUE = "user_queue";
    public static final String DEVICE_SYNC_QUEUE = "device_queue";

    @Bean
    public Queue userQueue() {
        // Al doilea parametru 'true' indică faptul că este DURABLE
        return new Queue(USER_SYNC_QUEUE, true);
    }

    @Bean
    public Queue deviceQueue() {
        // Al doilea parametru 'true' indică faptul că este DURABLE
        return new Queue(DEVICE_SYNC_QUEUE, true);
    }

    // Convertorul pentru a primi mesaje JSON și a le transforma în obiecte Java
    /*@Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }*/

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();

        // Mapează clasa care vine din Devices către clasa locală din Monitoring
        idClassMapping.put("com.example.devices.dtos.DeviceSyncDTO", com.example.monitoring.dtos.DeviceSyncDTO.class);

        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }
}