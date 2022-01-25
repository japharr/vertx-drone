package com.jeliiadesina.drone.util;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class RestApiUtil {
    public static void restResponse(RoutingContext ctx, int statusCode, String body) {
        ctx.response().putHeader("Content-Type", "application/json; charset=utf-8");
        ctx.response().setStatusCode(statusCode)
            .end(body);
    }

    public static void restResponse(RoutingContext ctx, int statusCode) {
        restResponse(ctx, statusCode, "");
    }

    public static <T> T decodeBodyToObject(RoutingContext ctx, Class<T> clazz) {
        try {
            return Json.decodeValue(ctx.getBodyAsString("UTF-8"), clazz);
        } catch (DecodeException exception) {
            ctx.fail(exception);
            return null;
        }
    }
}
