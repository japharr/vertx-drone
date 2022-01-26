package com.jeliiadesina.drone.api;

import com.jeliiadesina.drone.api.handler.DroneApi;
import com.jeliiadesina.drone.api.handler.HttpRequestValidator;
import com.jeliiadesina.drone.database.service.DroneDatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jeliiadesina.drone.api.Endpoints.*;

public class WebVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebVerticle.class);

    private static final String HTTP_KEY = "http";
    private static final String PORT_KEY = "port";
    private static final String EB_ADDRESSES = "eb.addresses";
    private static final String EB_DB_DRONE_ADDRESS = "db.drone";


    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        var droneDbEbAddress = config()
            .getJsonObject(EB_ADDRESSES).getString(EB_DB_DRONE_ADDRESS);

        var droneDatabaseService = DroneDatabaseService.createProxy(vertx, droneDbEbAddress);


        startServer(droneDatabaseService)
            .onComplete(r -> {
                if(r.succeeded())
                    startPromise.complete();
                else
                    startPromise.fail(r.cause());
            });
    }

    private Future<HttpServer> startServer(DroneDatabaseService droneDatabaseService) {
        int httpServerPort = config().getJsonObject(HTTP_KEY).getInteger(PORT_KEY);

        var httpServer = vertx.createHttpServer();
        var bodyHandler = BodyHandler.create();
        var router = Router.router(vertx);

        router.post().handler(bodyHandler);
        router.put().handler(bodyHandler);

        router.get(DRONE_GET_DRONES).handler(DroneApi.getAll(droneDatabaseService));
        router.get(DRONE_GET_DRONE_BY_SERIALNUMBER).handler(DroneApi.getBySerialNumber(droneDatabaseService));

        router.post(DRONE_REGISTER_NEW_DRONE)
            .handler(HttpRequestValidator.validateDrone())
            .handler(DroneApi.register(droneDatabaseService));

        return httpServer.
            requestHandler(router)
            .listen(httpServerPort);
    }
}