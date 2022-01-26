package com.jeliiadesina.drone.api.model;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.jeliiadesina.drone.entity.enumeration.Model;
import com.jeliiadesina.drone.util.EnumeratedConstraint;

public record DroneDto(String serialNumber, String model, Double weightLimit, Double batteryCapacity) {

    static EnumeratedConstraint<Model> modelConstraint = EnumeratedConstraint.enumValues(Model.class);

    public static final Validator<DroneDto> validator = ValidatorBuilder.<DroneDto>of()
        .constraint(DroneDto::serialNumber, "serialNumber", c -> c.notNull().notBlank())
        .constraint(DroneDto::model, "model", c -> c.notNull().predicate(modelConstraint))
        .constraint(DroneDto::weightLimit, "weightLimit", c -> c.notNull().lessThanOrEqual(500.0).greaterThanOrEqual(0.0))
        .constraint(DroneDto::batteryCapacity, "batteryCapacity", c -> c.notNull().greaterThanOrEqual(0.0).lessThanOrEqual(100.0))
        .build();

}