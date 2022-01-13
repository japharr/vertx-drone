package com.jeliiadesina.drone.web.handler;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class MedicationHandler {
  private final EventBus eventBus;
  private final JsonObject httpConf = new JsonObject();
  private final JsonObject i10nConf = new JsonObject();

  public MedicationHandler(EventBus eventBus, JsonObject config) {
    this.eventBus = eventBus;

    httpConf.mergeIn(config.getJsonObject("http"));
    i10nConf.mergeIn(config.getJsonObject("i18n"));
  }

  public void fetchAllMedications(RoutingContext ctx) {

  }

  public void createMedication(RoutingContext ctx) {

  }
}
