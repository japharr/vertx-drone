package com.jeliiadesina.drone.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface MedicationRepository {
  Future<Integer> countByName(String name);

  Future<JsonObject> findByName(String name);

  Future<JsonArray> findByDroneId(String droneId);

  Future<Double> totalDroneWeigh(String droneId);

  Future<JsonArray> findAll();

  Future<JsonObject> persistMedication(JsonObject data);

  Future<JsonObject> updateImage(JsonObject data);

  Future<JsonObject> updateDroneId(JsonObject data);
}
