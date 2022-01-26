package com.jeliiadesina.drone.api;

public final class Endpoints {
  public static final String DRONE_GET_DRONES = "/drones";
  public static final String DRONE_REGISTER_NEW_DRONE = "/register";
  public static final String DRONE_GET_DRONE_BY_SERIALNUMBER = "/drones/:serialNumber";
  public static final String DRONE_GET_MEDICATIONS_BY_DRONE_SERIALNUMBER = "/drones/:serialNumber/medications";
  public static final String DRONE_ADD_MEDICATION_TO_DRONE= "/drones/:serialNumber/medications";

  public static final String MEDICATION_GET_MEDICATIONS = "/medications";
  public static final String MEDICATION_REGISTER_NEW_MEDICATION = "/medications";
  public static final String MEDICATION_GET_MEDICATION_BY_NAME = "/medications/:name";

  private Endpoints() {}
}
