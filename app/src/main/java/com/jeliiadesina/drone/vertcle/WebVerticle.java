package com.jeliiadesina.drone.vertcle;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import com.jeliiadesina.drone.web.handler.DroneHandler;
import com.jeliiadesina.drone.web.handler.MedicationHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.validation.BadRequestException;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.Bodies;
import io.vertx.json.schema.SchemaParser;
import io.vertx.json.schema.SchemaRouter;
import io.vertx.json.schema.SchemaRouterOptions;
import io.vertx.json.schema.common.dsl.ObjectSchemaBuilder;

import static io.vertx.json.schema.common.dsl.Schemas.*;

public class WebVerticle extends AbstractVerticle {
  private final JsonObject httpConf = new JsonObject();
  private final JsonObject i10nConf = new JsonObject();
  private SchemaParser schemaParser;
  private DroneHandler droneHandler;
  private MedicationHandler medicationHandler;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    httpConf.mergeIn(config().getJsonObject("http"));
    i10nConf.mergeIn(config().getJsonObject("i18n"));

    droneHandler = new DroneHandler(vertx.eventBus(), config());
    medicationHandler = new MedicationHandler(vertx.eventBus(), config());

    startWebApp()
        .onComplete(http ->
            completeStartup(http, startPromise));
  }

  private void completeStartup(AsyncResult<HttpServer> http, Promise<Void> startPromise) {
    if (http.succeeded()) {
      startPromise.complete();
    } else {
      startPromise.fail(http.cause());
    }
  }

  private Future<HttpServer> startWebApp() {
    Router router = Router.router(vertx);
    SchemaRouter schemaRouter = SchemaRouter.create(vertx, new SchemaRouterOptions());
    schemaParser = SchemaParser.createDraft201909SchemaParser(schemaRouter);

    BodyHandler bodyHandler = BodyHandler.create();
    router.post().handler(bodyHandler);
    router.put().handler(bodyHandler);

    var basePath = httpConf.getString("context-path", "/api/v1");
    var port = httpConf.getInteger("port", 8080);

    router.get(basePath + "/drones").handler(droneHandler::loadAllDrones);
    router.get(basePath + "/drones/:serialNumber").handler(droneHandler::getBySerialNumber);
    router.get(basePath + "/drones/:serialNumber/medications").handler(medicationHandler::getBySerialNumber);
    router.post(basePath + "/drones")
        .handler(registerDroneValidationHandler())
        .handler(droneHandler::validateRegistration)
        .handler(droneHandler::registerDrone);

    router.post(basePath + "/medications/:name/image")
        .handler(BodyHandler.create());

    router.get(basePath + "/medications").handler(medicationHandler::fetchAllMedications);
    router.get(basePath + "/medications/:name").handler(medicationHandler::getByName);
    router.post(basePath + "/medications/:name/image").handler(medicationHandler::imageUpload);
    router.post(basePath + "/medications")
        .handler(createMedicationValidationHandler())
        .handler(medicationHandler::validateAndVerify)
        .handler(medicationHandler::createMedication);

    router.errorHandler(400, ctx -> {
      if (ctx.failure() instanceof BadRequestException) {
        ctx.response().setStatusCode(400).end(((BadRequestException) ctx.failure()).toJson().encodePrettily());
      }
    });

    return Future.future(promise -> vertx.createHttpServer()
        .requestHandler(router)
        .listen(port, promise));
  }

  private ValidationHandler registerDroneValidationHandler() {
    ObjectSchemaBuilder bodySchemaBuilder = objectSchema()
        .property(Drone.SERIAL_NUMBER, stringSchema())
        .property(Drone.MODEL, stringSchema())
        .property(Drone.WEIGHT_LIMIT, numberSchema())
        .property(Drone.BATTERY_CAPACITY, numberSchema());

    return ValidationHandler
        .builder(schemaParser)
        .body(Bodies.json(bodySchemaBuilder))
        .body(Bodies.formUrlEncoded(bodySchemaBuilder)).build();
  }

  private ValidationHandler createMedicationValidationHandler() {
    ObjectSchemaBuilder bodySchemaBuilder = objectSchema()
        .property(Medication.NAME, stringSchema())
        .property(Medication.CODE, stringSchema())
        .property(Medication.WEIGHT, numberSchema());

    return ValidationHandler
        .builder(schemaParser)
        .body(Bodies.json(bodySchemaBuilder))
        .body(Bodies.formUrlEncoded(bodySchemaBuilder)).build();
  }
}
