package com.jeliiadesina.drone.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface DroneRepository {
  Future<Integer> countDroneBySerialNumber(String serialNumber);

  Future<JsonObject> persistDrone(JsonObject data);
}
