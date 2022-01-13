package com.jeliiadesina.drone.repository;

import com.jeliiadesina.drone.entity.Drone;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface DroneRepository {
  Future<Integer> countDroneBySerialNumber(String serialNumber);

  Future<JsonObject> findDroneBySerialNumber(String serialNumber);

  Future<JsonArray> findAllDrones();

  Future<JsonArray> findDronesByState(Drone.StateType state);

  Future<JsonObject> persistDrone(JsonObject data);
}
