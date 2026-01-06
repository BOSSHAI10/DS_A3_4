package com.example.auth.services;

import com.example.auth.dtos.new_users.NewUserDTO;
import com.example.auth.dtos.new_users.NewUserDetailsDTO;
import com.example.auth.dtos.new_users.builders.NewUserBuilder;
import com.example.auth.entities.NewUser;
import com.example.auth.handlers.model.ResourceNotFoundException;
import com.example.auth.repositories.NewUserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NewUserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewUserService.class);
    private final NewUserRepository userRepository;

    @Autowired
    public NewUserService(NewUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<NewUserDTO> findUsers() {
        List<NewUser> userList = userRepository.findAll();
        return userList.stream()
                .map(NewUserBuilder::toUserDTO)
                .collect(Collectors.toList());
    }

    public NewUserDetailsDTO findUserById(UUID id) {
        Optional<NewUser> prosumerOptional = userRepository.findById(id);
        if (prosumerOptional.isEmpty()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(NewUser.class.getSimpleName() + " with id: " + id);
        }
        return NewUserBuilder.toUserDetailsDTO(prosumerOptional.get());
    }

    public NewUserDetailsDTO findUserByEmail(String email) {
        Optional<NewUser> prosumerOptional = userRepository.findByEmail(email);
        if (prosumerOptional.isEmpty()) {
            LOGGER.error("Person with email {} was not found in db", email);
            throw new ResourceNotFoundException(NewUser.class.getSimpleName() + " with email: " + email);
        }
        return NewUserBuilder.toUserDetailsDTO(prosumerOptional.get());
    }

    @Transactional
    public UUID insert(NewUserDetailsDTO userDetailsDTO) {
        NewUser user = NewUserBuilder.toEntity(userDetailsDTO);
        user = userRepository.save(user);
        LOGGER.debug("Person with id {} was inserted in db", user.getId());
        return user.getId();
    }

    @Transactional
    public NewUser register(NewUserDetailsDTO userDetailsDTO) {
        NewUser user = NewUserBuilder.toEntity(userDetailsDTO);
        user = userRepository.save(user);
        LOGGER.debug("Person with id {} was inserted in db", user.getId());
        return user;
    }

    @Transactional
    public NewUser register(String name, String email, Integer age) {
        NewUser user = new NewUser(name, email, age);
        user = userRepository.save(user);
        LOGGER.debug("Person with id {} was inserted in db", user.getId());
        return user;
    }

    public boolean existsById (UUID id) {
        Optional<NewUser> prosumerOptional = userRepository.findById(id);
        return prosumerOptional.isPresent();
    }

    @Transactional
    public void remove(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public NewUserDetailsDTO updateFully(UUID id, @Valid NewUserDetailsDTO dto) {
        // Dacă dto e invalid, Spring aruncă automat ConstraintViolationException
        NewUser entity = null;
        try {
            entity = userRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        } catch (ChangeSetPersister.NotFoundException e) {
            LOGGER.error("User with id {} was not found in db", id);
            throw new RuntimeException(e);
        }
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setAge(dto.getAge());
        return NewUserBuilder.toUserDetailsDTO(userRepository.save(entity));
    }
}
