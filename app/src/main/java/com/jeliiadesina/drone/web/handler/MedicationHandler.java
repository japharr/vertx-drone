package com.jeliiadesina.drone.web.handler;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.regex.Pattern;

import static com.jeliiadesina.drone.util.ErrorUtil.errorField;
import static com.jeliiadesina.drone.util.LocaleMessageUtil.getMessage;
import static com.jeliiadesina.drone.util.RoutingContextUtil.getLanguageKey;
import static com.jeliiadesina.drone.util.RoutingContextUtil.jsonBody;

public class MedicationHandler {
  private final Pattern validName = Pattern.compile("^[A-Za-z0-9_-]*$");
  private final Pattern validCode = Pattern.compile("^[A-Z0-9_]*$");

  private final EventBus eventBus;
  private final JsonObject httpConf = new JsonObject();
  private final JsonObject i10nConf = new JsonObject();

  public MedicationHandler(EventBus eventBus, JsonObject config) {
    this.eventBus = eventBus;

    httpConf.mergeIn(config.getJsonObject("http"));
    i10nConf.mergeIn(config.getJsonObject("i18n"));
  }

  public void createMedication(RoutingContext ctx) {
    JsonObject medication = Medication.object(jsonBody(ctx));

    eventBus.request(Medication.CREATE_ADDRESS, medication, res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200).end(medication.encodePrettily());
      } else {
        ReplyException cause = (ReplyException) res.cause();
        String failMessage = cause.getMessage();
        ctx.response().setStatusCode(400).end(failMessage);
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

  public void validateAndVerify(RoutingContext ctx) {
    JsonObject body = jsonBody(ctx);

    JsonArray requireFields = verify(body, ctx);
    if(!requireFields.isEmpty()) {
      JsonObject error = new JsonObject().put("required", requireFields);
      ctx.response().setStatusCode(400).end(error.encodePrettily());
      return;
    }

    JsonArray validatedFields = validate(body, ctx);
    if (!validatedFields.isEmpty()) {
      JsonObject error = new JsonObject().put("validation", validatedFields);
      ctx.response().setStatusCode(400).end(error.encodePrettily());
      return;
    }

    ctx.next();
  }

  private JsonArray verify(JsonObject body, RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();

    if(!body.containsKey(Medication.NAME))
      jsonArray.add(errorField(Medication.NAME, getMessage(getLanguageKey(ctx, i10nConf), "medication.name.required")));
    if(!body.containsKey(Medication.WEIGHT))
      jsonArray.add(errorField(Medication.WEIGHT, getMessage(getLanguageKey(ctx, i10nConf), "medication.weight.required")));
    if(!body.containsKey(Medication.CODE))
      jsonArray.add(errorField(Medication.CODE, getMessage(getLanguageKey(ctx, i10nConf), "medication.code.required")));

    return jsonArray;
  }

  private JsonArray validate(JsonObject body, RoutingContext ctx) {
    JsonArray jsonArray = new JsonArray();

    String name = body.getString(Medication.NAME);
    if(validateName(name)) {
      jsonArray.add(errorField(Medication.NAME, getMessage(getLanguageKey(ctx, i10nConf), "medication.ame.invalid")));
    }

    String code = body.getString(Medication.CODE);
    if(validateCode(code)) {
      jsonArray.add(errorField(Medication.CODE, getMessage(getLanguageKey(ctx, i10nConf), "medication.code.invalid")));
    }

    return jsonArray;
  }

  private boolean validateName(String name) {
    return !validName.matcher(name).matches();
  }

  private boolean validateCode(String code) {
    return !validCode.matcher(code).matches();
  }
}
