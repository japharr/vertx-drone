package com.jeliiadesina.drone.entity;

import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public interface Drone {
  // field-name
  public static final String SERIAL_NUMBER = "serialNumber";
  public static final String MODEL = "model";
  public static final String WEIGHT_LIMIT = "weightLimit";
  public static final String BATTERY_CAPACITY = "batteryCapacity";
  public static final String STATE = "state";

  // validation
  public static final int SERIAL_NUMBER_MAX = 100;
  public static final List<String> ACCEPTABLE_MODELS =
      List.of("Lightweight", "Middleweight", "Cruiserweight", "Heavyweight");
  public static final double WEIGHT_LIMIT_MAX = 500.0;

  // event-buss addresses
  public static final String REGISTER_ADDRESS = "drone.register";
  public static final String FETCH_ALL_ADDRESS = "drone.fetch-all";
  public static final String FETCH_BY_STATE_ADDRESS = "drone.fetch-state";

  public enum StateType{
    IDLE, LOADING, LOADED, DELIVERING, DELIVERED, RETURNING
  }

  // error codes
  static Map<Integer, String> ERROR_CODES = Map.of(101, "drone.serialNumber.exist");

  // construct Drone object
  static JsonObject droneObject(JsonObject body) {
    return new JsonObject()
        .put(SERIAL_NUMBER, body.getString(SERIAL_NUMBER))
        .put(MODEL, body.getString(MODEL))
        .put(WEIGHT_LIMIT, body.getDouble(WEIGHT_LIMIT))
        .put(BATTERY_CAPACITY, body.getDouble(BATTERY_CAPACITY))
        .put(STATE, StateType.IDLE);
  }
  static String insertDrone() {
    return "INSERT INTO drones VALUES($1, $2, $3, $4, $5, $6, current_timestamp, current_timestamp)";
  }

  static String countBySerialNumber() {
    return "SELECT count(*) FROM drones WHERE serial_number = $1";
  }

  static String selectOneBySerialNumber() {
    return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones " +
        "WHERE serial_number = $1 LIMIT 1";
  }

  static String selectAllDrones() {
    return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones";
  }

  static String selectDronesByState() {
    return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones " +
        "WHERE state = $1";
  }
}
