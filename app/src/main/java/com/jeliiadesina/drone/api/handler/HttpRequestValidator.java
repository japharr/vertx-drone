package com.jeliiadesina.drone.api.handler;

import com.jeliiadesina.drone.entity.Drone;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.jeliiadesina.drone.util.RestApiUtil.decodeBodyToObject;
import static com.jeliiadesina.drone.util.RestApiUtil.restResponse;

public class HttpRequestValidator {
    public static Handler<RoutingContext> validateDrone() {
        return ctx -> {
            var drone = decodeBodyToObject(ctx, Drone.class);

            if(drone == null) {
                ctx.fail(400);
                return;
            }

            var violations = Drone.validator.validate(drone);
            if(violations.isValid()) {
                ctx.next();
                return;
            }

            JsonArray array = new JsonArray();
            violations.forEach(r -> {
                array.add(new JsonObject().put(r.messageKey(), r.message()));
            });

            restResponse(ctx, 400, array.encodePrettily());
        };
    }
}
