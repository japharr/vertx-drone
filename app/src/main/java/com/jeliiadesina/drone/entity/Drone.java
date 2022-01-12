package com.jeliiadesina.drone.entity;

import java.util.List;

public abstract class Drone {
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

  public static final String REGISTER_ADDRESS = "drone.register";

  public enum StateType{
    IDLE, LOADING, LOADED, DELIVERING, DELIVERED, RETURNING
  }
}
