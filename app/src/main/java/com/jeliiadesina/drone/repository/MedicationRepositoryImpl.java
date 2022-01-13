package com.jeliiadesina.drone.repository;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.UUID;

import static com.jeliiadesina.drone.entity.Drone.countBySerialNumber;
import static com.jeliiadesina.drone.entity.Drone.insertDrone;
import static com.jeliiadesina.drone.entity.Medication.countByNameQuery;
import static com.jeliiadesina.drone.entity.Medication.insertOneQuery;

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
  public Future<JsonObject> persistMedication(JsonObject data) {
    String name = data.getString(Medication.NAME);
    return countByName(name)
        .compose(count -> persist(count, data));
  }

  private Future<JsonObject> persist(int count, JsonObject data) {
    if(count > 0) {
      return Future.failedFuture("medication.name.exist");
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
}
