package com.jeliiadesina.drone.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
@JsonPropertyOrder({"id", "name", "weight", "code", "image"})
public class Medication {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("weight")
    private Double weight;
    @JsonProperty("code")
    private String code;
    @JsonProperty("image")
    private String image;
    @JsonProperty("droneId")
    private String droneId;

    public Medication () {}

    public Medication (String json) {
        this(new JsonObject(json));
    }

    public Medication (JsonObject jsonObject) {
        MedicationConverter.fromJson(jsonObject, this);
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        MedicationConverter.toJson(this, jsonObject);
        return jsonObject;
    }

    public Medication (Medication other) {
        this.id = other.id;
        this.name = other.name;
        this.weight = other.weight;
        this.code = other.code;
        this.image = other.image;
        this.droneId = other.droneId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    @Override
    public String toString() {
        return "{" +
            "\"name\" : \"" + name + "\"," +
            "\"weight\" : " + weight + "," +
            "\"code\" : \"" + code + "\"," +
            "\"image\" : \"" + image + "\"" +
            "}";
    }
}
