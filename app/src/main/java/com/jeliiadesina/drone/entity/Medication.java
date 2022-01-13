package com.jeliiadesina.drone.entity;

import io.vertx.core.json.JsonObject;

public interface Medication {
  // field-name
  String NAME = "name";
  String WEIGHT = "weight";
  String CODE = "code";
  String IMAGE = "image";

  // event-buss addresses
  String CREATE_ADDRESS = "medication.create";
  String FETCH_ALL_ADDRESS = "medication.fetch-all";
  String FETCH_BY_STATE_ADDRESS = "drone.fetch-state";


  // construct Drone object
  static JsonObject object(JsonObject body) {
    return new JsonObject()
        .put(NAME, body.getString(NAME))
        .put(WEIGHT, body.getDouble(WEIGHT))
        .put(CODE, body.getString(CODE))
        .put(IMAGE, body.getString(IMAGE));
  }
}
