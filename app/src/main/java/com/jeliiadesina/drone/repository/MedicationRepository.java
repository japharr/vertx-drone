package com.jeliiadesina.drone.repository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface MedicationRepository {
  Future<Integer> countByName(String name);

  Future<JsonObject> persistMedication(JsonObject data);
}
