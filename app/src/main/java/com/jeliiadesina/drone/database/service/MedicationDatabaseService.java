package com.jeliiadesina.drone.database.service;

import com.jeliiadesina.drone.database.service.impl.MedicationDatabaseServiceImpl;
import com.jeliiadesina.drone.entity.Medication;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;

@ProxyGen
public interface MedicationDatabaseService {
    static MedicationDatabaseService create(PgPool pgPool, Handler<AsyncResult<MedicationDatabaseService>> resultHandler) {
        return new MedicationDatabaseServiceImpl(pgPool, resultHandler);
    }

    static MedicationDatabaseService createProxy(Vertx vertx, String address) {
        return new MedicationDatabaseServiceVertxEBProxy(vertx, address);
    }

    Future<Void> persist(Medication medication);
    Future<JsonObject> findById(String id);
    Future<Medication> findByName(String name);
    Future<JsonArray> findAll();
    Future<JsonArray> findAllByDroneId(String droneId);
    Future<Double> totalDroneWeigh(String droneId);
    Future<Void> updateMedicationWithDrone(String medicationId, String droneId);
    Future<Void> updateMedicationWithImage(String medicationId, String imagePath);
}
