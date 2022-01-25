package com.jeliiadesina.drone.repository;

import com.jeliiadesina.drone.entity.Drone01;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface DroneRepository {
  Future<Integer> countDroneBySerialNumber(String serialNumber);

  Future<JsonObject> findDroneBySerialNumber(String serialNumber);

  Future<JsonArray> findAllDrones();

  Future<JsonObject> findById(String id);

  Future<JsonArray> findDronesByState(Drone01.StateType state);

  Future<JsonArray> findDAvailableDrones();

  Future<JsonObject> persistDrone(JsonObject data);

  Future<Drone01.StateType> updateState(String droneId, Drone01.StateType state);
}
