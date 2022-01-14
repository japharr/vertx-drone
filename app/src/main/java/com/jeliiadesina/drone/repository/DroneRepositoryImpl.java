package com.jeliiadesina.drone.repository;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.exception.NotFoundException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.UUID;

import static com.jeliiadesina.drone.entity.Drone.*;

public class DroneRepositoryImpl implements DroneRepository{
  private final SqlClient sqlClient;

  public DroneRepositoryImpl(SqlClient sqlClient) {
    this.sqlClient = sqlClient;
  }

  @Override
  public Future<Integer> countDroneBySerialNumber(String serialNumber) {
    return sqlClient
        .preparedQuery(countBySerialNumber())
        .execute(Tuple.of(serialNumber))
        .map(rs -> rs.iterator().next())
        .flatMap(item -> {
          var count = item.getInteger(0);
          return Future.future(p -> p.complete(count != null? count : 0));
        });
  }

  @Override
  public Future<JsonObject> findDroneBySerialNumber(String serialNumber) {
    return sqlClient
        .preparedQuery(selectOneBySerialNumber())
        .execute(Tuple.of(serialNumber))
        .compose(this::mapToFirstResult);
  }

  Future<JsonObject> mapToFirstResult(RowSet<Row> rs) {
    if (rs.size() >= 1) {
     return Future.future(p -> p.complete(mapToJsonObject(rs.iterator().next())));
    } else {
      return Future.<JsonObject>failedFuture(new NotFoundException("drone.serialNumber.not-found"));
    }
  }

  @Override
  public Future<JsonObject> findById(String id) {
    return sqlClient
        .preparedQuery(selectOneById())
        .execute(Tuple.of(id))
        .compose(this::mapToFirstResult);
  }

  @Override
  public Future<JsonArray> findAllDrones() {
    return sqlClient
        .preparedQuery(selectAllDrones())
        .execute()
        .flatMap(this::mapToJsonArray);
  }

  @Override
  public Future<JsonArray> findDronesByState(StateType state) {
    return sqlClient
        .preparedQuery(selectDronesByState())
        .execute(Tuple.of(state))
        .flatMap(this::mapToJsonArray);
  }

  @Override
  public Future<JsonObject> persistDrone(JsonObject data) {
    String serialNumber = data.getString(Drone.SERIAL_NUMBER);
    return countDroneBySerialNumber(serialNumber)
        .compose(count -> persistDrone(count, data));
  }

  private Future<JsonObject> persistDrone(int count, JsonObject data) {
    if(count > 0) {
      return Future.failedFuture("drone.serialNumber.exist");
    }

    Tuple values = Tuple.of(
        UUID.randomUUID().toString(),
        data.getString(Drone.SERIAL_NUMBER),
        data.getString(Drone.MODEL),
        data.getDouble(Drone.WEIGHT_LIMIT),
        data.getDouble(Drone.BATTERY_CAPACITY),
        data.getString(Drone.STATE));

    return sqlClient
        .preparedQuery(insertDrone())
        .execute(values)
        .map(rs -> data);
  }

  private JsonObject mapToJsonObject(Row row) {
    return new JsonObject()
        .put("id", row.getValue("id"))
        .put(SERIAL_NUMBER, row.getValue("serial_number"))
        .put(MODEL, row.getValue("model"))
        .put(WEIGHT_LIMIT, row.getValue("weight_limit"))
        .put(BATTERY_CAPACITY, row.getValue("battery_capacity"))
        .put(STATE, row.getValue("state"));
  }

  private Future<JsonArray> mapToJsonArray(RowSet<Row> rows) {
    JsonArray data = new JsonArray();
    rows.forEach(row -> data.add(mapToJsonObject(row)));
    return Future.succeededFuture(data);
  }

}
