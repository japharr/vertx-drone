package com.jeliiadesina.drone.web.handler;

import com.jeliiadesina.drone.entity.Drone;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.jeliiadesina.drone.util.ErrorUtil.errorField;
import static com.jeliiadesina.drone.util.LocaleMessageUtil.getMessage;
import static com.jeliiadesina.drone.util.RoutingContextUtil.getLanguageKey;
import static com.jeliiadesina.drone.util.RoutingContextUtil.jsonBody;

public class DroneHandler {
  private final EventBus eventBus;
  private final JsonObject httpConf = new JsonObject();
  private final JsonObject i10nConf = new JsonObject();

  public DroneHandler(EventBus eventBus, JsonObject config) {
    this.eventBus = eventBus;

    httpConf.mergeIn(config.getJsonObject("http"));
    i10nConf.mergeIn(config.getJsonObject("i18n"));
  }

  public void registerDrone(RoutingContext ctx) {
    JsonObject drone = Drone.droneObject(jsonBody(ctx));

    eventBus.request(Drone.REGISTER_ADDRESS, drone, res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200).end(drone.encodePrettily());
      } else {
        ReplyException cause = (ReplyException) res.cause();
        String failMessage = cause.getMessage();
        ctx.response().setStatusCode(400).end(getMessage(getLanguageKey(ctx, i10nConf), failMessage));
      }
    });
  }

  public void loadAllDrones(RoutingContext ctx) {
    String state = ctx.queryParams().get("state");
    if(state != null) {
      loadByState(ctx);
      return;
    }

    eventBus.request(Drone.FETCH_ALL_ADDRESS, new JsonObject(), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonArray)res.result().body()).encodePrettily());
      } else {
        ctx.fail(500);
      }
    });
  }

  public void loadByState(RoutingContext ctx) {
    String state = ctx.queryParams().get("state");
    eventBus.request(Drone.FETCH_BY_STATE_ADDRESS, new JsonObject().put("state", state), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonArray)res.result().body()).encodePrettily());
      } else {
        ctx.fail(500);
      }
    });
  }

  public void loadById(RoutingContext ctx) {
    String id = ctx.pathParam("id");
    eventBus.request(Drone.FETCH_BY_ID_ADDRESS, new JsonObject().put("id", id), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonObject)res.result().body()).encodePrettily());
      } else {
        ctx.fail(500);
      }
    });
  }

  public void getBySerialNumber(RoutingContext ctx) {
    String serialNumber = ctx.pathParam("serialNumber");
    eventBus.request(Drone.FETCH_BY_SERIAL_NUMBER_ADDRESS, new JsonObject().put(Drone.SERIAL_NUMBER, serialNumber), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonObject)res.result().body()).encodePrettily());
      } else {
        ReplyException cause = (ReplyException) res.cause();
        ctx.response().setStatusCode(cause.failureCode())
            .end(getMessage(getLanguageKey(ctx, i10nConf), cause.getMessage(), serialNumber));
      }
    });
  }

  public void validateRegistration(RoutingContext ctx) {
    JsonObject body = jsonBody(ctx);

    JsonArray requireFields = anyRegistrationFieldIsMissing(body, ctx);
    if(!requireFields.isEmpty()) {
      JsonObject error = new JsonObject().put("required", requireFields);
      ctx.response().setStatusCode(400).end(error.encodePrettily());
      return;
    }

    JsonArray validatedFields = anyRegistrationFieldIsWrong(body, ctx);
    if (!validatedFields.isEmpty()) {
      JsonObject error = new JsonObject()
          .put("validation", validatedFields);
      ctx.response().setStatusCode(400).end(error.encodePrettily());
      return;
    }

    ctx.next();
  }

  private JsonArray anyRegistrationFieldIsMissing(JsonObject body, RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();

    if(!body.containsKey(Drone.SERIAL_NUMBER))
      jsonArray.add(errorField(Drone.SERIAL_NUMBER, getMessage(getLanguageKey(ctx, i10nConf), "serialNumber.required")));
    if(!body.containsKey(Drone.MODEL))
      jsonArray.add(errorField(Drone.MODEL, getMessage(getLanguageKey(ctx, i10nConf), "model.required")));
    if(!body.containsKey(Drone.WEIGHT_LIMIT))
      jsonArray.add(errorField(Drone.WEIGHT_LIMIT, getMessage(getLanguageKey(ctx, i10nConf), "weightLimit.required")));
    if(!body.containsKey(Drone.BATTERY_CAPACITY))
      jsonArray.add(errorField(Drone.BATTERY_CAPACITY, getMessage(getLanguageKey(ctx, i10nConf), "batteryCapacity.required")));

    return jsonArray;
  }

  private JsonArray anyRegistrationFieldIsWrong(JsonObject body, RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();

    String serialNumber = body.getString(Drone.SERIAL_NUMBER);
    if(serialNumber.length() > Drone.SERIAL_NUMBER_MAX) {
      jsonArray.add(errorField(Drone.SERIAL_NUMBER, getMessage(getLanguageKey(ctx, i10nConf), "serialNumber.max", Drone.SERIAL_NUMBER_MAX)));
    }

    String model = body.getString(Drone.MODEL);
    if(!Drone.ACCEPTABLE_MODELS.contains(model)) {
      jsonArray.add(errorField(Drone.MODEL, getMessage(getLanguageKey(ctx, i10nConf), "model.acceptable", String.join(", ", Drone.ACCEPTABLE_MODELS))));
    }

    double weightLimit = body.getDouble(Drone.WEIGHT_LIMIT, 0.0);
    if(weightLimit > Drone.WEIGHT_LIMIT_MAX) {
      jsonArray.add(errorField(Drone.WEIGHT_LIMIT, getMessage(getLanguageKey(ctx, i10nConf), "weightLimit.max", Drone.WEIGHT_LIMIT_MAX)));
    }

    double batteryCapacity = body.getDouble(Drone.BATTERY_CAPACITY, 0.0);
    if(batteryCapacity > 100) {
      jsonArray.add(errorField(Drone.BATTERY_CAPACITY, getMessage(getLanguageKey(ctx, i10nConf), "batteryCapacity.max", Drone.BATTERY_CAPACITY_MAX)));
    }

    return jsonArray;
  }
}
