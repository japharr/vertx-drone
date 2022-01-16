package com.jeliiadesina.drone;

import com.jeliiadesina.drone.vertcle.DatabaseVerticle;
import com.jeliiadesina.drone.vertcle.WebVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private static Vertx vertx;
  private static PostgreSQLContainer<?> postgres;

  public static void main(String[] args) {
    postgres = new PostgreSQLContainer<>("postgres:13.4")
        .withDatabaseName("test").withUsername("test").withPassword("test");
    postgres.start();

    vertx = Vertx.vertx();

    initConfig()
        .compose(App::deployVerticle)
        .onComplete(deployed -> {
          if (deployed.succeeded()) {
            logger.info("Verticle(s) successfully deployed");
          } else {
            logger.error("Failed to deploy verticle(s)", deployed.cause());
          }
        });
  }

  private static Future<JsonObject> initConfig() {
    var storeOptions = new ConfigStoreOptions()
        .setFormat("yaml").setType("file")
        .setConfig(new JsonObject().put("path", "conf.yaml"));

    var retrievalOptions = new ConfigRetrieverOptions()
        .addStore(storeOptions);

    var configRetriever = ConfigRetriever.create(vertx, retrievalOptions);

    return configRetriever.getConfig();
  }

  private static Future<Void> deployVerticle(JsonObject config) {
    var dbConf = testContainerPostgresDbConf();
    var deploymentOpts = new DeploymentOptions()
        .setConfig(config.mergeIn(dbConf));

    var webVerticle = vertx.deployVerticle(new WebVerticle(), deploymentOpts);
    var dbVerticle = vertx.deployVerticle(new DatabaseVerticle(), deploymentOpts);

    return CompositeFuture.all(dbVerticle, webVerticle).mapEmpty();
  }

  private static JsonObject testContainerPostgresDbConf() {
    return new JsonObject()
        .put("db", new JsonObject()
            .put("host", postgres.getContainerIpAddress())
            .put("port", postgres.getMappedPort(5432))
            .put("database", postgres.getDatabaseName())
            .put("user", postgres.getUsername())
            .put("password", postgres.getPassword()));
  }
}
