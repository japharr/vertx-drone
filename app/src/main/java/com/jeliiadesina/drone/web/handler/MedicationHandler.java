package com.jeliiadesina.drone.web.handler;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.jeliiadesina.drone.util.LocaleMessageUtil.getMessage;
import static com.jeliiadesina.drone.util.RoutingContextUtil.getLanguageKey;
import static com.jeliiadesina.drone.util.RoutingContextUtil.jsonBody;

public class MedicationHandler {
  private final EventBus eventBus;
  private final JsonObject httpConf = new JsonObject();
  private final JsonObject i10nConf = new JsonObject();

  public MedicationHandler(EventBus eventBus, JsonObject config) {
    this.eventBus = eventBus;

    httpConf.mergeIn(config.getJsonObject("http"));
    i10nConf.mergeIn(config.getJsonObject("i18n"));
  }

  public void createMedication(RoutingContext ctx) {
    JsonObject drone = Medication.object(jsonBody(ctx));

    eventBus.request(Medication.CREATE_ADDRESS, drone, res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200).end(drone.encodePrettily());
      } else {
        ReplyException cause = (ReplyException) res.cause();
        String failMessage = cause.getMessage();
        ctx.response().setStatusCode(400).end(getMessage(getLanguageKey(ctx, i10nConf), failMessage));
      }
    });
  }

  public void fetchAllMedications(RoutingContext ctx) {
    eventBus.request(Medication.FETCH_ALL_ADDRESS, new JsonObject(), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonArray)res.result().body()).encodePrettily());
      } else {
        ctx.fail(500);
      }
    });
  }
}
