package com.jeliiadesina.drone.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface MedicationRepository {
  Future<Integer> countByName(String name);

  Future<JsonArray> findAll();

  Future<JsonObject> persistMedication(JsonObject data);
}