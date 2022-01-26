package com.jeliiadesina.drone.api.handler;

import com.jeliiadesina.drone.database.service.DroneDatabaseService;
import com.jeliiadesina.drone.database.service.MedicationDatabaseService;
import com.jeliiadesina.drone.entity.Medication;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static com.jeliiadesina.drone.util.RestApiUtil.decodeBodyToObject;
import static com.jeliiadesina.drone.util.RestApiUtil.restResponse;

public class MedicationApi {
    public static Handler<RoutingContext> getAll(MedicationDatabaseService medicationDatabaseService) {
        return ctx -> {
            medicationDatabaseService.findAll()
                .onComplete(res -> {
                    if(res.succeeded()) {
                        restResponse(ctx, 200, res.result().encode());
                    } else {
                        restResponse(ctx, 500, res.cause().getMessage());
                    }
                });
        };
    }

    public static Handler<RoutingContext> getByName(MedicationDatabaseService medicationDatabaseService) {
        return ctx -> {
            String name = ctx.pathParam("name");

            medicationDatabaseService.findByName(name)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        restResponse(ctx, 200, res.result().encode());
                    } else {
                        restResponse(ctx, 500, res.cause().getMessage());
                    }
                });
        };
    }

    public static Handler<RoutingContext> create(MedicationDatabaseService medicationDatabaseService) {
        return ctx -> {
            Medication medication = decodeBodyToObject(ctx, Medication.class);

            medicationDatabaseService.persist(medication)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        restResponse(ctx, 200);
                    } else {
                        restResponse(ctx, 500, res.cause().getMessage());
                    }
                });
        };
    }

    public static Handler<RoutingContext> getByDroneSerialNumber(MedicationDatabaseService medicationDatabaseService, DroneDatabaseService droneDatabaseService) {
        return ctx -> {
            String serialNumber = ctx.pathParam("serialNumber");

            droneDatabaseService.findBySerialNumber(serialNumber)
                .compose(drone -> medicationDatabaseService.findAllByDroneId(drone.getId()))
                .onComplete(res -> {
                    if(res.succeeded()) {
                        restResponse(ctx, 200, res.result().encode());
                    } else {
                        restResponse(ctx, 500, res.cause().getMessage());
                    }
                });
        };
    }
}
