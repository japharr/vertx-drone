package com.jeliiadesina.drone.entity;

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

  public enum StateType{
    IDLE, LOADING, LOADED, DELIVERING, DELIVERED, RETURNING
  }

  // error codes
  static Map<Integer, String> ERROR_CODES = Map.of(101, "drone.serialNumber.exist");

  static String insertDrone() {
    return "INSERT INTO drones VALUES($1, $2, $3, $4, $5, $6, current_timestamp, current_timestamp)";
  }

  static String countBySerialNumber() {
    return "SELECT count(*) FROM drones WHERE serial_number = $1";
  }
}
