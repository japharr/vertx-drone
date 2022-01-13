package com.jeliiadesina.drone.util;

import io.vertx.core.json.JsonObject;

public abstract class ErrorUtil {
  public static JsonObject errorField(String fieldName, String message) {
    return new JsonObject().put(fieldName, message);
  }
}
