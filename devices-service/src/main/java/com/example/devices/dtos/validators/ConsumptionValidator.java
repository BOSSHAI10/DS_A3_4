package com.example.devices.dtos.validators;


import com.example.devices.dtos.validators.annotation.ConsumptionLimit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConsumptionValidator implements ConstraintValidator<ConsumptionLimit, Integer> {
    private int min;
    @Override public void initialize(ConsumptionLimit ann) { this.min = ann.value(); }
    @Override public boolean isValid(Integer age, ConstraintValidatorContext ctx) {
        if (age == null) return true;               // let @NotNull enforce presence
        return age >= min;
    }
}


