package com.jeliiadesina.drone.vertcle;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.repository.DroneRepository;
import com.jeliiadesina.drone.repository.DroneRepositoryImpl;
import com.jeliiadesina.drone.service.DroneService;
import com.jeliiadesina.drone.service.DroneServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);
  private DroneService droneService;

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
    vertx.eventBus().consumer(Drone.REGISTER_ADDRESS).handler(droneService::registerDrone);

    return Future.future(Promise::complete);
  }

  private Future<Void> configureSqlClient(Void unused) {
    var poolOptions = new PoolOptions().setMaxSize(5);
    var connectOptions = connectOptions();

    SqlClient sqlClient = Pool.pool(vertx, connectOptions, poolOptions);
    droneService = new DroneServiceImpl(new DroneRepositoryImpl(sqlClient));

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
