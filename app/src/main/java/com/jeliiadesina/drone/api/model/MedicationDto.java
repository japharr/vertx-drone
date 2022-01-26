package com.jeliiadesina.drone.api.model;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;

public record MedicationDto (String name, Double weight, String code) {
    private static final String CODE_PATTERN = "^[A-Z0-9_]*$";
    private static final String NAME_PATTERN = "^[A-Za-z0-9_-]*$";

    public static final Validator<MedicationDto> validator = ValidatorBuilder.<MedicationDto>of()
        .constraint(MedicationDto::name, "serialNumber", c -> c.notNull().notBlank().pattern(NAME_PATTERN).message("Only letters, numbers, hyphen and underscore are allowed"))
        .constraint(MedicationDto::weight, "weightLimit", c -> c.notNull().greaterThanOrEqual(0.0))
        .constraint(MedicationDto::code, "model", c -> c.notNull().notBlank().pattern(CODE_PATTERN).message("Only upper case letters, underscore and numbers are allowed"))
        .build();
}