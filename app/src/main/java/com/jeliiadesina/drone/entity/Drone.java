package com.jeliiadesina.drone.entity;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.jeliiadesina.drone.entity.enumeration.Model;
import com.jeliiadesina.drone.entity.enumeration.State;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
@JsonPropertyOrder({"serialNumber", "model", "weightLimit", "batteryCapacity", "state"})
public class Drone {
    @JsonProperty("serialNumber")
    private String serialNumber;
    @JsonProperty("model")
    private Model model;
    @JsonProperty("weightLimit")
    private Double weightLimit;
    @JsonProperty("batteryCapacity")
    private Double batteryCapacity;
    @JsonProperty("state")
    private State state;

    public static final Validator<Drone> validator = ValidatorBuilder.<Drone>of()
        .constraint(Drone::getSerialNumber, "username", c -> c.notNull())
        .constraint(Drone::getWeightLimit, "weightLimit", c -> c.notNull().lessThanOrEqual(500.0).greaterThanOrEqual(0.0))
        .constraint(Drone::getBatteryCapacity, "batteryCapacity", c -> c.notNull().greaterThanOrEqual(0.0).lessThanOrEqual(100.0))
        .build();

    public Drone () {}

    public Drone (String json) {
        this(new JsonObject(json));
    }

    public Drone (JsonObject jsonObject) {
        DroneConverter.fromJson(jsonObject, this);
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        DroneConverter.toJson(this, jsonObject);
        return jsonObject;
    }

    public Drone (Drone other) {
        this.serialNumber = other.serialNumber;
        this.model = other.model;
        this.weightLimit = other.weightLimit;
        this.batteryCapacity = other.batteryCapacity;
        this.state = other.state;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Double getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(Double weightLimit) {
        this.weightLimit = weightLimit;
    }

    public Double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
