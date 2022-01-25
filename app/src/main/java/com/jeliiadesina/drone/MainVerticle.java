package com.jeliiadesina.drone;

import com.jeliiadesina.drone.api.WebVerticle;
import com.jeliiadesina.drone.database.DatabaseVerticle;
import com.jeliiadesina.drone.migration.MigrationVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        initConfig()
            .compose(this::deployVerticle)
            .onComplete(deployed -> {
                if (deployed.succeeded()) {
                    LOGGER.info("Verticle(s) successfully deployed");
                    startPromise.complete();
                } else {
                    LOGGER.error("Failed to deploy verticle(s)", deployed.cause());
                    startPromise.fail(deployed.cause());
                }
            });
    }


    private Future<JsonObject> initConfig() {
        var storeOptions = new ConfigStoreOptions()
            .setFormat("yaml").setType("file")
            .setConfig(new JsonObject().put("path", "conf.yaml"));

        var retrievalOptions = new ConfigRetrieverOptions()
            .addStore(storeOptions);

        var configRetriever = ConfigRetriever.create(vertx, retrievalOptions);

        return configRetriever.getConfig();
    }

    private Future<Void> deployVerticle(JsonObject config) {
        var deploymentOpts = new DeploymentOptions()
            .setConfig(config);

//        var webVerticle = vertx.deployVerticle(new WebVerticle(), deploymentOpts);
//        var migrationVerticle = vertx.deployVerticle(new MigrationVerticle(), deploymentOpts);
//        var dbVerticle = vertx.deployVerticle(new DatabaseVerticle(), deploymentOpts);

//        return CompositeFuture.all(migrationVerticle, dbVerticle, webVerticle).mapEmpty();

        return vertx.deployVerticle(new MigrationVerticle(), deploymentOpts)
            .compose(id -> vertx.deployVerticle(new DatabaseVerticle(), deploymentOpts))
            .compose(id -> vertx.deployVerticle(new WebVerticle(), deploymentOpts)).mapEmpty();
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}
