package com.example.users.services;

import com.example.users.dtos.AuthDTO;
import com.example.users.dtos.UserDTO;
import com.example.users.dtos.UserDetailsDTO;
import com.example.users.dtos.builders.UserBuilder;
import com.example.users.entities.User;
import com.example.users.handlers.exceptions.model.ResourceNotFoundException;
import com.example.users.repositories.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate; // Injectează RabbitTemplate

    @Autowired
    public UserService(UserRepository userRepository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<UserDTO> findUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO findUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));
        return UserBuilder.toUserDetailsDTO(user);
    }

    public UserDetailsDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(User.class.getSimpleName() + " with email: " + email));
        return UserBuilder.toUserDetailsDTO(user);
    }

    @Transactional
    public UUID insert(UserDetailsDTO userDetailsDTO) {
        Optional<User> existingUser = userRepository.findByEmail(userDetailsDTO.getEmail());
        if (existingUser.isPresent()) {
            LOGGER.warn("Userul cu emailul {} exista deja. Se sare peste insert.", userDetailsDTO.getEmail());
            return existingUser.get().getId();
        }

        User user = UserBuilder.toEntity(userDetailsDTO);
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted in db", user.getId());

        try {
            String authUrl = "http://auth-service:8083/auth/register";
            String roleToSend = (userDetailsDTO.getRole() != null) ? userDetailsDTO.getRole().name() : "USER";
            AuthDTO authPayload = new AuthDTO(user.getEmail(), "1234", roleToSend);
            restTemplate.postForEntity(authUrl, authPayload, String.class);
        } catch (Exception e) {
            LOGGER.error("FAILED to sync user with Auth Service: " + e.getMessage());
        }
        rabbitTemplate.convertAndSend("user_queue", user.getId());
        return user.getId();
    }

    // --- METODA CARE LIPSEA ---
    @Transactional
    public void restoreUsers(List<UserDetailsDTO> userList) {
        int count = 0;
        for (UserDetailsDTO dto : userList) {
            if (!userRepository.existsByEmail(dto.getEmail())) {
                User user = UserBuilder.toEntity(dto);
                userRepository.save(user); // Doar salvare locală, fără Auth Sync
                count++;
            }
        }
        LOGGER.info("Restore completed: {} users added.", count);

    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    @Transactional
    public void remove(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));

        String email = user.getEmail();
        userRepository.deleteById(id);

        try {
            String authUrl = "http://auth-service:8083/auth/delete/" + email;
            restTemplate.delete(authUrl);
        } catch (Exception e) {
            LOGGER.error("FAILED to delete credentials from Auth Service: " + e.getMessage());
        }
        rabbitTemplate.convertAndSend("user_queue", id);
    }

    @Transactional
    public UserDetailsDTO updateFully(UUID id, @Valid UserDetailsDTO dto) {
        User entity = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id));

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setAge(dto.getAge());
        entity.setRole(dto.getRole());

        rabbitTemplate.convertAndSend("user_queue", entity.getId());

        return UserBuilder.toUserDetailsDTO(userRepository.save(entity));
    }
}