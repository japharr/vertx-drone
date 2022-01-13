package com.jeliiadesina.drone.repository;

import com.jeliiadesina.drone.entity.Drone;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.UUID;

import static com.jeliiadesina.drone.entity.Drone.countBySerialNumber;
import static com.jeliiadesina.drone.entity.Drone.insertDrone;

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

}
