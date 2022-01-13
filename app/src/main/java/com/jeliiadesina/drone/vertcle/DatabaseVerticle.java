package com.jeliiadesina.drone.vertcle;

import com.jeliiadesina.drone.entity.Drone;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.jeliiadesina.drone.entity.Drone.countBySerialNumber;
import static com.jeliiadesina.drone.entity.Drone.insertDrone;

public class DatabaseVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);
  private SqlClient sqlClient;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    doDatabaseMigrations()
        .compose(this::configureSqlClient)
        .compose(this::configureEventBusConsumers)
        .onComplete(res -> {
          if(res.succeeded()) {
            logger.info("DatabaseVerticle is up and running");
            startPromise.complete();
          } else {
            logger.error("Failed to start DatabaseVerticle", res.cause());
            startPromise.fail(res.cause());
          }
        });
  }

  private Future<Void> configureEventBusConsumers(Void unused) {
    vertx.eventBus().consumer(Drone.REGISTER_ADDRESS).handler(this::registerDrone);

    return Future.future(Promise::complete);
  }

  private Future<Integer> countDroneBySerialNumber(String serialNumber) {
    return sqlClient
        .preparedQuery(countBySerialNumber())
        .execute(Tuple.of(serialNumber))
        .map(rs -> rs.iterator().next())
        .flatMap(item -> {
          var count = item.getInteger(0);
          return Future.future(p -> p.complete(count != null? count : 0));
        });
  }

  private void registerDrone(Message<Object> msg) {
    if (msg.body() instanceof JsonObject data) {
      countDroneBySerialNumber(data.getString(Drone.SERIAL_NUMBER))
          .compose(r -> persistDrone(r, msg))
          .onComplete(rx -> {
            if(rx.succeeded()) {
              msg.reply(data);
            } else {
              msg.fail(500, rx.cause().getLocalizedMessage());
            }
          });
    } else {
      msg.fail(400, "json not formatted");
    }
  }

  private Future<JsonObject> persistDrone(int count, Message<Object> msg) {
    if(count > 0) {
      msg.fail(101, "drone.serialNumber.exist");
      return Future.failedFuture("drone.serialNumber.exist");
    }

    JsonObject data = (JsonObject) msg.body();

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

  private Future<Void> configureSqlClient(Void unused) {
    var poolOptions = new PoolOptions().setMaxSize(5);
    var connectOptions = connectOptions();

    sqlClient = Pool.pool(vertx, connectOptions, poolOptions);
    return Future.future(Promise::complete);
  }

  private PgConnectOptions connectOptions() {
    JsonObject db = config().getJsonObject("db", new JsonObject());

    return new PgConnectOptions()
        .setPort(db.getInteger("port"))
        .setHost(db.getString("host"))
        .setDatabase(db.getString("database"))
        .setUser(db.getString("user"))
        .setPassword(db.getString("password"));
  }

  private Future<Void> doDatabaseMigrations() {
    JsonObject dbConfig = config().getJsonObject("db", new JsonObject());

    String host = dbConfig.getString("host");
    int port = dbConfig.getInteger("port");
    String database = dbConfig.getString("database");
    String user = dbConfig.getString("user");
    String password = dbConfig.getString("password", "");

    String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

    Flyway flyway = Flyway.configure()
        .dataSource(url, user, password)
        .load();

    try {
      flyway.migrate();
      return Future.succeededFuture();
    } catch (FlywayException fe) {
      return Future.failedFuture(fe);
    }
  }
}
