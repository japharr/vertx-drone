package com.jeliiadesina.drone.database.service.impl;

import com.jeliiadesina.drone.database.service.DroneDatabaseService;
import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.enumeration.State;
import com.jeliiadesina.drone.exception.NotFoundException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.jeliiadesina.drone.query.DroneQuery.*;

public class DroneDatabaseServiceImpl implements DroneDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroneDatabaseServiceImpl.class);

    private final PgPool pgPool;

    public DroneDatabaseServiceImpl(PgPool pgPool, Handler<AsyncResult<DroneDatabaseService>> resultHandler) {
        this.pgPool = pgPool;

        this.pgPool.getConnection()
            .flatMap(pgConnection -> pgConnection
                .query(count())
                .execute()
                .eventually(r -> pgConnection.close()))
            .onSuccess(res -> {
                resultHandler.handle(Future.succeededFuture(this));
            })
            .onFailure(throwable -> {
                LOGGER.error("Unable to connect to database", throwable);
                resultHandler.handle(Future.failedFuture(throwable));
            });
    }


    @Override
    public Future<Void> persist(Drone drone) {
        var values = Tuple.of(
            UUID.randomUUID().toString(),
            drone.getSerialNumber(),
            drone.getModel(),
            drone.getWeightLimit(),
            drone.getBatteryCapacity(),
            State.IDLE
        );

        return pgPool
            .preparedQuery(insertDrone())
            .execute(values)
            .compose(rs -> Future.succeededFuture());
    }

    @Override
    public Future<JsonObject> findById(String id) {
        return pgPool
            .preparedQuery(selectOneById())
            .execute(Tuple.of(id))
            .compose(this::mapToFirstResult);
    }

    @Override
    public Future<JsonObject> findBySerialNumber(String serialNumber) {
        return pgPool
            .preparedQuery(selectOneBySerialNumber())
            .execute(Tuple.of(serialNumber))
            .compose(this::mapToFirstResult);
    }

    @Override
    public Future<JsonArray> findAll() {
        return pgPool
            .preparedQuery(selectAll())
            .execute()
            .flatMap(rs -> Future.succeededFuture(mapToJsonArray(rs)));
    }

    private JsonObject mapToJsonObject(Row row) {
        return new JsonObject()
            .put("id", row.getValue("id"))
            .put("serialNumber", row.getValue("serial_number"))
            .put("model", row.getValue("model"))
            .put("weightLimit", row.getValue("weight_limit"))
            .put("batteryCapacity", row.getValue("battery_capacity"))
            .put("state", row.getValue("state"));
    }

    private JsonArray mapToJsonArray(RowSet<Row> rows) {
        JsonArray data = new JsonArray();
        rows.forEach(row -> data.add(mapToJsonObject(row)));
        return data;
    }

    private Future<JsonObject> mapToFirstResult(RowSet<Row> rs) {
        if (rs.size() >= 1) {
            return Future.future(p -> p.complete(mapToJsonObject(rs.iterator().next())));
        } else {
            return Future.failedFuture(new NotFoundException(404, "drone.serialNumber.not-found"));
        }
    }
}
