package com.jeliiadesina.drone.api.handler;

import com.jeliiadesina.drone.api.model.DroneDto;
import com.jeliiadesina.drone.api.model.MedicationDto;
import com.jeliiadesina.drone.entity.Drone;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.jeliiadesina.drone.util.RestApiUtil.decodeBodyToObject;
import static com.jeliiadesina.drone.util.RestApiUtil.restResponse;
import static com.jeliiadesina.drone.util.ValidationUtils.extract;

public class HttpRequestValidator {
    public static Handler<RoutingContext> validateDrone() {
        return ctx -> {
            var drone = decodeBodyToObject(ctx, DroneDto.class);

            if(drone == null) {
                ctx.fail(400);
                return;
            }

            var violations = DroneDto.validator.validate(drone);
            if(violations.isValid()) {
                ctx.next();
                return;
            }

            restResponse(ctx, 400, extract(violations).encodePrettily());
        };
    }

    public static Handler<RoutingContext> validateMedication() {
        return ctx -> {
            var medication = decodeBodyToObject(ctx, MedicationDto.class);

            if(medication == null) {
                ctx.fail(400);
                return;
            }

            var violations = MedicationDto.validator.validate(medication);
            if(violations.isValid()) {
                ctx.next();
                return;
            }

            restResponse(ctx, 400, extract(violations).encodePrettily());
        };
    }
}
