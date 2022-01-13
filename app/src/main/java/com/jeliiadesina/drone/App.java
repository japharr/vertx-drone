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

public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private static Vertx vertx;

  public static void main(String[] args) {
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
    var deploymentOpts = new DeploymentOptions().setConfig(config);

    var webVerticle = vertx.deployVerticle(new WebVerticle(), deploymentOpts);
    var dbVerticle = vertx.deployVerticle(new DatabaseVerticle(), deploymentOpts);

    return CompositeFuture.all(dbVerticle, webVerticle).mapEmpty();
  }
}
