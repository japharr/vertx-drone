package com.jeliiadesina.drone.entity;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.jeliiadesina.drone.entity.enumeration.Model;
import com.jeliiadesina.drone.entity.enumeration.State;

import com.jeliiadesina.drone.util.EnumeratedConstraint;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
@JsonPropertyOrder({"id", "serialNumber", "model", "weightLimit", "batteryCapacity", "state"})
public class Drone {
    @JsonProperty("id")
    private String id;
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
        this.id = other.id;
        this.serialNumber = other.serialNumber;
        this.model = other.model;
        this.weightLimit = other.weightLimit;
        this.batteryCapacity = other.batteryCapacity;
        this.state = other.state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getModelStr() {
        if(model == null) return null;

        return model.name();
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
    }@Override

    public String toString() {
        return "{" +
            "\"serialNumber\" : \"" + serialNumber + "\"," +
            "\"model\" : \"" + model + "\"," +
            "\"weightLimit\" : " + weightLimit + "," +
            "\"batteryCapacity\" : " + batteryCapacity + "," +
            "\"state\" : \"" + state + "\"" +
            "}";
    }

    public static class Builder {
        private final Drone drone;

        public Builder() {
            drone = new Drone();
        }

        public Builder id(String id) {
            drone.setId(id);
            return this;
        }

        public Builder serialNumber(String serialNumber) {
            drone.setSerialNumber(serialNumber);
            return this;
        }

        public Builder model(Model model) {
            drone.setModel(model);
            return this;
        }

        public Builder weightLimit(Double weightLimit) {
            drone.setWeightLimit(weightLimit);
            return this;
        }

        public Builder batteryCapacity(Double batteryCapacity) {
            drone.setBatteryCapacity(batteryCapacity);
            return this;
        }

        public Builder state(State state) {
            drone.setState(state);
            return this;
        }

        public Drone build() {
            return drone;
        }
    }
}
