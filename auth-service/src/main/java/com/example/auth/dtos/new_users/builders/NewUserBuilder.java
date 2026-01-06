package com.example.auth.dtos.new_users.builders;

import com.example.auth.dtos.new_users.NewUserDTO;
import com.example.auth.dtos.new_users.NewUserDetailsDTO;
import com.example.auth.entities.NewUser;

public class NewUserBuilder {
    private NewUserBuilder() {
    }

    public static NewUserDTO toUserDTO(NewUser user) {
        return new NewUserDTO(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }

    public static NewUserDetailsDTO toUserDetailsDTO(NewUser user) {
        return new NewUserDetailsDTO(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }

    public static NewUser toEntity(NewUserDetailsDTO userDetailsDTO) {
        return new NewUser(userDetailsDTO.getName(),
                userDetailsDTO.getEmail(),
                userDetailsDTO.getAge());
    }
}
