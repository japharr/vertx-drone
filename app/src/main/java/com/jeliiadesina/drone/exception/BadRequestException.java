package com.jeliiadesina.drone.exception;

public class BadRequestException extends RuntimeException {
  public BadRequestException(Throwable throwable) {
    super(throwable);
  }

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
