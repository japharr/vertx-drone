package com.jeliiadesina.drone.web.handler;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Pattern;

import static com.jeliiadesina.drone.util.ErrorUtil.errorField;
import static com.jeliiadesina.drone.util.LocaleMessageUtil.getMessage;
import static com.jeliiadesina.drone.util.RoutingContextUtil.*;

public class MedicationHandler {
  private final static Logger logger = LoggerFactory.getLogger(MedicationHandler.class);

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
        handleEventBusException(ctx, i10nConf, res.cause());
      }
    });
  }

  public void fetchAllMedications(RoutingContext ctx) {
    eventBus.request(Medication.FETCH_ALL_ADDRESS, new JsonObject(), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonArray)res.result().body()).encodePrettily());
      } else {
        handleEventBusException(ctx, i10nConf, res.cause());
      }
    });
  }

  public void getByName(RoutingContext ctx) {
    String name = ctx.pathParam("name");
    eventBus.request(Medication.FETCH_BY_NAME_ADDRESS, new JsonObject().put(Medication.NAME, name), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonObject)res.result().body()).encodePrettily());
      } else {
        handleEventBusException(ctx, i10nConf, res.cause(), name);
      }
    });
  }

  public void getBySerialNumber(RoutingContext ctx) {
    String serialNumber = ctx.pathParam("serialNumber");
    eventBus.request(Medication.FETCH_BY_SERIAL_NUMBER_ADDRESS, new JsonObject().put(Drone.SERIAL_NUMBER, serialNumber), res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200)
            .end(((JsonArray)res.result().body()).encodePrettily());
      } else {
        handleEventBusException(ctx, i10nConf, res.cause(), serialNumber);
      }
    });
  }

  public void addMedication(RoutingContext ctx) {
    String serialNumber = ctx.pathParam("serialNumber");
    JsonObject medicationWithDroneSn = Medication.objectMedicationName(jsonBody(ctx))
        .put(Drone.SERIAL_NUMBER, serialNumber);

    eventBus.request(Medication.ADD_MEDICATION_TO_DRONE_ADDRESS, medicationWithDroneSn, res -> {
      if(res.succeeded()) {
        ctx.response().setStatusCode(200).end(medicationWithDroneSn.encodePrettily());
      } else {
        handleEventBusException(ctx, i10nConf, res.cause());
      }
    });
  }

  public void imageUpload(RoutingContext ctx) {
    String name = ctx.pathParam(Medication.NAME);
    Optional<FileUpload> opt = ctx.fileUploads().stream().findFirst();
    if(opt.isPresent() && opt.get().contentType().contains("image")) {
      FileUpload fileUpload = opt.get();
      eventBus.request(Medication.UPLOAD_IMAGE_ADDRESS,
          new JsonObject()
              .put("name", name)
              .put("image", fileUpload.uploadedFileName())
              .put("fileName", fileUpload.fileName())
              .put("fileUploadName", fileUpload.name())
              .put("contentType", fileUpload.contentType()),
          res -> {
            if(res.succeeded()) {
              ctx.response().setStatusCode(200).end();
            } else {
              ctx.response().setStatusCode(500).end(res.cause().getMessage());
            }
          }
      );
    } else {
     ctx.response().setStatusCode(400).end("Please, upload an image");
    }
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
