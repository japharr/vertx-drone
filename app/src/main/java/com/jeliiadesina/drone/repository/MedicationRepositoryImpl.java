package com.jeliiadesina.drone.repository;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import com.jeliiadesina.drone.exception.AlreadyExistException;
import com.jeliiadesina.drone.exception.NotFoundException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.UUID;

import static com.jeliiadesina.drone.entity.Medication.*;

public class MedicationRepositoryImpl implements MedicationRepository {
  private final SqlClient sqlClient;

  public MedicationRepositoryImpl(SqlClient sqlClient) {
    this.sqlClient = sqlClient;
  }

  @Override
  public Future<Integer> countByName(String name) {
    return sqlClient
        .preparedQuery(countByNameQuery())
        .execute(Tuple.of(name))
        .map(rs -> rs.iterator().next())
        .flatMap(item -> {
          var count = item.getInteger(0);
          return Future.future(p -> p.complete(count != null? count : 0));
        });
  }

  @Override
  public Future<JsonObject> findByName(String name) {
    return sqlClient
        .preparedQuery(selectOneByName())
        .execute(Tuple.of(name))
        .compose(this::mapToFirstResult);
  }

  @Override
  public Future<JsonArray> findByDroneId(String droneId) {
    return sqlClient
        .preparedQuery(selectAllByDroneId())
        .execute(Tuple.of(droneId))
        .flatMap(this::mapToJsonArray);
  }

  @Override
  public Future<Double> totalDroneWeigh(String droneId) {
    return sqlClient
        .preparedQuery(selectTotalMedicationWeigh())
        .execute(Tuple.of(droneId))
        .compose(this::mapToTotalDroneWeigh);
  }

  private Future<Double> mapToTotalDroneWeigh(RowSet<Row> rs) {
    Double total = 0.0;
    if (rs.size() >= 1) {
      var row = rs.iterator().next();
      total = row.getDouble("total_weight");
    }
    return Future.succeededFuture(total);
  }

  //selectTotalMedicationWeigh

  @Override
  public Future<JsonArray> findAll() {
    return sqlClient
        .preparedQuery(selectAllQuery())
        .execute()
        .flatMap(this::mapToJsonArray);
  }

  @Override
  public Future<JsonObject> persistMedication(JsonObject data) {
    String name = data.getString(Medication.NAME);
    return countByName(name)
        .compose(count -> persist(count, data));
  }

  private Future<JsonObject> persist(int count, JsonObject data) {
    if(count > 0) {
      return Future.failedFuture(new AlreadyExistException(400, "medication.name.exist"));
    }

    Tuple values = Tuple.of(
        UUID.randomUUID().toString(),
        data.getString(Medication.NAME),
        data.getDouble(Medication.WEIGHT),
        data.getString(Medication.CODE));

    return sqlClient
        .preparedQuery(insertOneQuery())
        .execute(values)
        .map(rs -> data);
  }

  @Override
  public Future<JsonObject> updateImage(JsonObject data) {
    String name = data.getString(Medication.NAME);
    return countByName(name)
        .compose(count -> updateImage(count, data));
  }

  @Override
  public Future<JsonObject> updateDroneId(JsonObject data) {
    String name = data.getString(Medication.NAME);
    return countByName(name)
        .compose(count -> updateDroneId(count, data));
  }

  private Future<JsonObject> updateImage(int count, JsonObject data) {
    if(count == 0) {
      return Future.failedFuture(new NotFoundException(404, "medication.name.not-found"));
    }

    Tuple values = Tuple.of(
        data.getString(Medication.NAME),
        data.getString("image"));

    return sqlClient
        .preparedQuery(updateWithImage())
        .execute(values)
        .map(rs -> data);
  }

  private Future<JsonObject> updateDroneId(int count, JsonObject data) {
    if(count == 0) {
      return Future.failedFuture(new NotFoundException(404, "medication.name.not-found"));
    }

    Tuple values = Tuple.of(
        data.getString(Medication.NAME),
        data.getString("droneId"));

    return sqlClient
        .preparedQuery(updateWithDroneId())
        .execute(values)
        .map(rs -> data);
  }

  private Future<JsonObject> mapToFirstResult(RowSet<Row> rs) {
    if (rs.size() >= 1) {
      return Future.future(p -> p.complete(mapToJsonObject(rs.iterator().next())));
    } else {
      return Future.failedFuture(new NotFoundException(404, "medication.name.not-found"));
    }
  }

  private JsonObject mapToJsonObject(Row row) {
    return new JsonObject()
        .put("id", row.getValue("id"))
        .put(NAME, row.getValue("name"))
        .put(WEIGHT, row.getValue("weight"))
        .put(CODE, row.getValue("code"))
        .put(IMAGE, row.getValue("image"))
        .put(DRONE_ID, row.getValue("drone_id"));
  }

  private Future<JsonArray> mapToJsonArray(RowSet<Row> rows) {
    JsonArray data = new JsonArray();
    rows.forEach(row -> data.add(mapToJsonObject(row)));
    return Future.succeededFuture(data);
  }
}
