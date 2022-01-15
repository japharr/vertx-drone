package com.jeliiadesina.drone.exception;

public class DroneException extends RuntimeException {
  private int code = 400;

  public DroneException() {
    super();
  }

  public DroneException(String message) {
    super(message);
  }

  public DroneException(int code, String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }
}
