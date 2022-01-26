package com.jeliiadesina.drone.database;

import com.jeliiadesina.drone.config.PgConfig;
import com.jeliiadesina.drone.database.service.DroneDatabaseService;
import com.jeliiadesina.drone.database.service.MedicationDatabaseService;
import io.vertx.core.*;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVerticle.class);

    private static final String EB_ADDRESSES = "eb.addresses";
    private static final String EB_DB_DRONE_ADDRESS = "db.drone";
    private static final String EB_DB_MEDICATION_ADDRESS = "db.medication";

    private PgPool pgPool;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOGGER.info("deploying DatabaseVerticle...,");

        PgConnectOptions connectOptions = PgConfig.pgConnectOpts(config());
        PoolOptions poolOptions = PgConfig.poolOptions(config());

        pgPool = PgPool.pool(vertx, connectOptions, poolOptions);

        String droneDbEbAddress = config().getJsonObject(EB_ADDRESSES).getString(EB_DB_DRONE_ADDRESS);
        String medicationDbEbAddress = config().getJsonObject(EB_ADDRESSES).getString(EB_DB_MEDICATION_ADDRESS);

        Promise<Void> dronePromise = Promise.promise();
        Promise<Void> medicationPromise = Promise.promise();

        DroneDatabaseService.create(pgPool, onCreate(droneDbEbAddress, dronePromise, DroneDatabaseService.class));
        MedicationDatabaseService.create(pgPool, onCreate(medicationDbEbAddress, medicationPromise, MedicationDatabaseService.class));

        CompositeFuture.all(dronePromise.future(), medicationPromise.future())
            .onSuccess(r -> startPromise.complete())
            .onFailure(rx -> startPromise.fail(rx.getCause()));
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if(pgPool != null)
            pgPool.close(r -> {
                if(r.succeeded()) stopPromise.complete();
                else stopPromise.fail(r.cause());
            });
    }

    private <T> Handler<AsyncResult<T>> onCreate(String dbEbAddress, Promise<Void> startPromise, Class<T> clazz) {
        return result -> {
            if(result.succeeded()) {
                LOGGER.info("succeeded");
                new ServiceBinder(vertx)
                    .setAddress(dbEbAddress)
                    .register(clazz, result.result())
                    .exceptionHandler(throwable -> {
                        LOGGER.error("Failed to establish PostgreSQL database service", throwable);
                        startPromise.fail(throwable);
                    })
                    .completionHandler(res -> {
                        LOGGER.info("PostgreSQL database service is successfully established in \"" + dbEbAddress + "\"");
                        startPromise.complete();
                    });
            } else {
                LOGGER.error("Failed to initiate the connection to database", result.cause());
                startPromise.fail(result.cause());
                Future.future(p -> startPromise.fail(result.cause()));
            }
        };
    }
}
