package com.jeliiadesina.drone.database.service;

import com.jeliiadesina.drone.database.service.impl.DroneDatabaseServiceImpl;
import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.enumeration.State;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;

import java.util.List;

@ProxyGen
public interface DroneDatabaseService {

    static DroneDatabaseService create(PgPool pgPool, Handler<AsyncResult<DroneDatabaseService>> resultHandler) {
        return new DroneDatabaseServiceImpl(pgPool, resultHandler);
    }

    static DroneDatabaseService createProxy(Vertx vertx, String address) {
        return new DroneDatabaseServiceVertxEBProxy(vertx, address);
    }

    Future<Void> persist(Drone drone);
    Future<JsonObject> findById(String id);
    Future<Drone> findBySerialNumber(String id);
    Future<JsonArray> findAll();
    Future<Integer> countById(String id);
    Future<State> updateState(String id, State state);
    Future<List<Drone>> findAllAvailable();
}
