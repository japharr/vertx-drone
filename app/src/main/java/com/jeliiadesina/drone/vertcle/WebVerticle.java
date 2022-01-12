package com.jeliiadesina.drone.vertcle;

import com.jeliiadesina.drone.entity.Drone;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class WebVerticle extends AbstractVerticle {
  private final JsonObject httpConf = new JsonObject();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    httpConf.mergeIn(config().getJsonObject("http"));

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

    BodyHandler bodyHandler = BodyHandler.create();
    router.post().handler(bodyHandler);
    router.put().handler(bodyHandler);

    var basePath = httpConf.getString("context-path", "/api/v1");
    var port = httpConf.getInteger("port", 8080);

    router.get(basePath + "/drones").handler(this::loadAllDrones);
    router.post(basePath + "/drones")
        .handler(this::validateRegistration)
        .handler(this::registerDrone);

    return Future.future(promise -> vertx.createHttpServer()
        .requestHandler(router)
        .listen(port, promise));
  }

  private void validateRegistration(RoutingContext ctx) {
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

  private void registerDrone(RoutingContext ctx) {

  }

  private void loadAllDrones(RoutingContext ctx) {

  }

  private JsonArray anyRegistrationFieldIsMissing(JsonObject body, RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();

    if(!body.containsKey(Drone.SERIAL_NUMBER))
      jsonArray.add(errorField(Drone.SERIAL_NUMBER, getMessage(ctx, "serialNumber.required")));
    if(!body.containsKey(Drone.MODEL))
      jsonArray.add(errorField(Drone.MODEL, getMessage(ctx, "model.required")));
    if(!body.containsKey(Drone.WEIGHT_LIMIT))
      jsonArray.add(errorField(Drone.WEIGHT_LIMIT, getMessage(ctx, "weightLimit.required")));
    if(!body.containsKey(Drone.BATTERY_CAPACITY))
      jsonArray.add(errorField(Drone.BATTERY_CAPACITY, getMessage(ctx, "batteryCapacity.required")));

    return jsonArray;
  }

  private JsonArray anyRegistrationFieldIsWrong(JsonObject body, RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();

    String serialNumber = body.getString(Drone.SERIAL_NUMBER);
    if(serialNumber.length() > Drone.SERIAL_NUMBER_MAX) {
      jsonArray.add(errorField(Drone.SERIAL_NUMBER, getMessage(ctx, "serialNumber.max", Drone.SERIAL_NUMBER_MAX)));
    }

    String model = body.getString(Drone.MODEL);
    if(!Drone.ACCEPTABLE_MODELS.contains(model)) {
      jsonArray.add(errorField(Drone.MODEL, getMessage(ctx, "model.acceptable", String.join(", ", Drone.ACCEPTABLE_MODELS))));
    }

    double weightLimit = body.getDouble(Drone.WEIGHT_LIMIT, 0.0);
    if(weightLimit > Drone.WEIGHT_LIMIT_MAX) {
      jsonArray.add(errorField(Drone.WEIGHT_LIMIT, getMessage(ctx, "weightLimit.max", Drone.WEIGHT_LIMIT_MAX)));
    }

    return jsonArray;
  }

  private JsonObject errorField(String fieldName, String message) {
    return new JsonObject().put(fieldName, message);
  }

  private JsonObject jsonBody(RoutingContext ctx) {
    if (ctx.getBody().length() == 0) {
      return new JsonObject();
    } else {
      return ctx.getBodyAsJson();
    }
  }

  private String getMessage(RoutingContext ctx, String key) {
    Optional<LanguageHeader> languageHeader = ctx.acceptableLanguages().stream().findFirst();

    String language = languageHeader.isPresent()? languageHeader.get().tag() : "en";

    Locale locale = Locale.forLanguageTag(language);

    if(locale == null) locale = Locale.ENGLISH;

    ResourceBundle labels = ResourceBundle.getBundle("messages", locale);

    return labels.getString(key);
  }

  private String getMessage(RoutingContext ctx, String key, Object... input) {
    return MessageFormat.format(getMessage(ctx, key), input);
  }
}
