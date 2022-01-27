package com.jeliiadesina.drone.api.handler;

import com.jeliiadesina.drone.database.service.DroneDatabaseService;
import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.exception.ResourceNotFoundException;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static com.jeliiadesina.drone.util.RestApiUtil.decodeBodyToObject;
import static com.jeliiadesina.drone.util.RestApiUtil.restResponse;

public class DroneApi {
    public static Handler<RoutingContext> getAll(DroneDatabaseService droneDatabaseService) {
        return ctx -> {
            droneDatabaseService.findAll()
                .onComplete(res -> {
                    if(res.succeeded()) {
                        restResponse(ctx, 200, res.result().encode());
                    } else {
                        restResponse(ctx, 500, res.cause().getMessage());
                    }
                });
        };
    }

    public static Handler<RoutingContext> getBySerialNumber(DroneDatabaseService droneDatabaseService) {
        return ctx -> {
            String serialNumber = ctx.pathParam("serialNumber");

            droneDatabaseService.findBySerialNumber(serialNumber)
                .onComplete(res -> {
                    if(res.succeeded()) {
                      if(res.result() == null)
                        ctx.fail(new ResourceNotFoundException(
                            String.format("Drone with serialNumber: %s, not found", serialNumber)));
                      else restResponse(ctx, 200, res.result().toString());
                    } else {
                        ctx.fail(res.cause());
                    }
                });
        };
    }

    public static Handler<RoutingContext> register(DroneDatabaseService droneDatabaseService) {
        return ctx -> {
            Drone drone = decodeBodyToObject(ctx, Drone.class);

            droneDatabaseService.persist(drone)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        restResponse(ctx, 200);
                    } else {
                        restResponse(ctx, 500, res.cause().getMessage());
                    }
                });
        };
    }
}
