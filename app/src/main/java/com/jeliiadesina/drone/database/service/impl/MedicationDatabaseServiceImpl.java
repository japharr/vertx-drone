package com.jeliiadesina.drone.database.service.impl;

import com.jeliiadesina.drone.database.service.MedicationDatabaseService;
import com.jeliiadesina.drone.entity.Medication;
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

import static com.jeliiadesina.drone.entity.Medication01.selectTotalMedicationWeigh;
import static com.jeliiadesina.drone.query.MedicationQuery.*;

public class MedicationDatabaseServiceImpl implements MedicationDatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroneDatabaseServiceImpl.class);

    private final PgPool pgPool;

    public MedicationDatabaseServiceImpl(PgPool pgPool, Handler<AsyncResult<MedicationDatabaseService>> resultHandler) {
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
    public Future<Void> persist(Medication medication) {
        var values = Tuple.of(
            UUID.randomUUID().toString(),
            medication.getName(),
            medication.getWeight(),
            medication.getCode()
        );

        return pgPool
            .preparedQuery(insertMedication())
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
    public Future<Medication> findByName(String name) {
        return pgPool
            .preparedQuery(selectOneByName())
            .execute(Tuple.of(name))
            .compose(this::mapToFirstResult)
            .compose(jsonObject -> Future.succeededFuture(mapToMedication(jsonObject)));
    }

    @Override
    public Future<JsonArray> findAll() {
        return pgPool
            .preparedQuery(selectAll())
            .execute()
            .flatMap(rs -> Future.succeededFuture(mapToJsonArray(rs)));
    }

    @Override
    public Future<JsonArray> findAllByDroneId(String droneId) {
        return pgPool
            .preparedQuery(selectAllByDroneId())
            .execute(Tuple.of(droneId))
            .flatMap(rs -> Future.succeededFuture(mapToJsonArray(rs)));
    }

    @Override
    public Future<Double> totalDroneWeigh(String droneId) {
        return pgPool
            .preparedQuery(selectTotalMedicationWeigh())
            .execute(Tuple.of(droneId))
            .compose(rows -> Future.succeededFuture(mapToTotalDroneWeigh(rows)));
    }

    @Override
    public Future<Void> updateMedicationWithDrone(String medicationId, String droneId) {
        return pgPool
            .preparedQuery(updateWithDroneId())
            .execute(Tuple.of(medicationId, droneId))
            .compose(r -> Future.succeededFuture());
    }

    private JsonObject mapToJsonObject(Row row) {
        return new JsonObject()
            .put("id", row.getValue("id"))
            .put("name", row.getValue("name"))
            .put("weight", row.getValue("weight"))
            .put("code", row.getValue("code"))
            .put("image", row.getValue("image"))
            .put("droneId", row.getValue("drone_id"));
    }

    private Medication mapToMedication(JsonObject jsonObject) {
        return new Medication(jsonObject);
    }

    private JsonArray mapToJsonArray(RowSet<Row> rows) {
        JsonArray data = new JsonArray();
        rows.forEach(row -> data.add(mapToJsonObject(row)));
        return data;
    }

    private Double mapToTotalDroneWeigh(RowSet<Row> rs) {
        Double total = 0.0;
        if (rs.size() >= 1) {
            var row = rs.iterator().next();
            total = row.getDouble("total_weight");
        }
        return total;
    }

    private Future<JsonObject> mapToFirstResult(RowSet<Row> rs) {
        if (rs.size() >= 1) {
            return Future.future(p -> p.complete(mapToJsonObject(rs.iterator().next())));
        } else {
            return Future.failedFuture(new NotFoundException(404, "medication.name.not-found"));
        }
    }
}
