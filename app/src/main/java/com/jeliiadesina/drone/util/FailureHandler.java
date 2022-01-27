package com.jeliiadesina.drone.util;

import com.jeliiadesina.drone.exception.BadRequestException;
import com.jeliiadesina.drone.exception.ResourceNotFoundException;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.jeliiadesina.drone.util.RestApiUtil.restResponse;

public class FailureHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext routingContext) {
    Throwable failure = routingContext.failure();
    if (failure instanceof BadRequestException) {
      restResponse(routingContext, 400, errorMessageToErrorBody(failure.getMessage()));
    } else if (failure instanceof ResourceNotFoundException) {
      restResponse(routingContext, 404, errorMessageToErrorBody(failure.getMessage()));
    } else if (failure instanceof DecodeException) {
      restResponse(routingContext, 400, errorMessageToErrorBody("Problems parsing JSON"));
    } else {
      restResponse(routingContext, 500, errorMessageToErrorBody(failure.getMessage()));
    }
  }

  private String errorMessageToErrorBody(String message) {
    return new JsonObject().put("message", message).toString();
  }
}