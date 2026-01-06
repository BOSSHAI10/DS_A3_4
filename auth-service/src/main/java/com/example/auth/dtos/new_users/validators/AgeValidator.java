package com.example.auth.dtos.new_users.validators;


import com.example.auth.dtos.new_users.validators.annotation.AgeLimit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeValidator implements ConstraintValidator<AgeLimit, Integer> {
    private int min;
    @Override public void initialize(AgeLimit ann) { this.min = ann.value(); }
    @Override public boolean isValid(Integer age, ConstraintValidatorContext ctx) {
        if (age == null) return true;               // let @NotNull enforce presence
        return age >= min;
    }
}


