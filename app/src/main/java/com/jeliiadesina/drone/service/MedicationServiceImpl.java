package com.jeliiadesina.drone.service;

import com.jeliiadesina.drone.entity.Drone01;
import com.jeliiadesina.drone.entity.Medication01;
import com.jeliiadesina.drone.exception.AlreadyExistException;
import com.jeliiadesina.drone.exception.DroneException;
import com.jeliiadesina.drone.exception.NotFoundException;
import com.jeliiadesina.drone.repository.DroneRepository;
import com.jeliiadesina.drone.repository.MedicationRepository;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MedicationServiceImpl implements MedicationService {
  private static final Logger logger = LoggerFactory.getLogger(MedicationService.class);
  private static final Set<Drone01.StateType> forbidden =
      Set.of(Drone01.StateType.DELIVERED, Drone01.StateType.DELIVERING,
          Drone01.StateType.RETURNING);

  private final MedicationRepository repository;
  private final DroneRepository droneRepository;

  public MedicationServiceImpl(MedicationRepository repository, DroneRepository droneRepository) {
    this.repository = repository;
    this.droneRepository = droneRepository;
  }

  @Override
  public void create(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    repository.persistMedication(data)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(data);
          } else {
            if(rx.cause() instanceof AlreadyExistException ex) {
              msg.fail(ex.getCode(), ex.getMessage());
            } else {
              msg.fail(500, rx.cause().getMessage());
            }
          }
        });
  }

  @Override
  public void updateImage(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    repository.updateImage(data)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(data);
          } else {
            msg.fail(500, rx.cause().getLocalizedMessage());
          }
        });
  }

  @Override
  public void fetchByName(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    String name = data.getString(Medication01.NAME);
    repository.findByName(name)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            if(rx.cause() instanceof NotFoundException ex) {
              msg.fail(ex.getCode(), ex.getMessage());
            } else {
              msg.fail(500, rx.cause().getMessage());
            }
          }
        });
  }

  @Override
  public void fetchByDrone(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    String serialNumber = data.getString(Drone01.SERIAL_NUMBER);
    droneRepository.findDroneBySerialNumber(serialNumber)
        .compose(json -> repository.findByDroneId(json.getString(Drone01.ID)))
        .onComplete(rx -> {
            if(rx.succeeded()) {
                msg.reply(rx.result());
            } else {
              if(rx.cause() instanceof NotFoundException ex) {
                msg.fail(ex.getCode(), ex.getMessage());
              } else {
                msg.fail(500, rx.cause().getMessage());
              }
            }
        });
  }

  @Override
  public void fetchAll(Message<Object> msg) {
    repository.findAll()
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            msg.fail(501, rx.cause().getMessage());
          }
        });
  }

  @Override
  public void addMedicationToDrone(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    String serialNumber = data.getString(Drone01.SERIAL_NUMBER);
    String name = data.getString(Medication01.NAME);

    CompositeFuture.all(
        repository.findByName(name),
        droneRepository.findDroneBySerialNumber(serialNumber)
    ).compose(r2 -> {
      var medication = (JsonObject) r2.resultAt(0);
      var drone = (JsonObject) r2.resultAt(1);
      JsonObject jsonObject = new JsonObject()
          .put("drone", drone).put("medication", medication);
      return Future.succeededFuture(jsonObject);
    }).compose(this::verifyDrone)
        .compose(this::verifyDroneWeight)
        .compose(this::loadMedicationToDrone)
        .compose(this::updateDroneState)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            if(rx.cause() instanceof NotFoundException ex) {
              msg.fail(ex.getCode(), ex.getMessage());
            } else if(rx.cause() instanceof DroneException ex) {
              msg.fail(ex.getCode(), ex.getMessage());
            } else {
              msg.fail(500, rx.cause().getMessage());
            }
          }
        });
  }

  private Future<JsonObject> loadMedicationToDrone(JsonObject object) {
    JsonObject medication = object.getJsonObject("medication");
    JsonObject drone = object.getJsonObject("drone");
    JsonObject data = new JsonObject()
        .put("newState", object.getString("newState"))
        .put(Medication01.NAME, medication.getString(Medication01.NAME))
            .put("droneId", drone.getString(Drone01.ID));

    return repository.updateDroneId(data)
        .compose(r -> Future.future(p -> p.complete(data)));
  }

  private Future<JsonObject> updateDroneState(JsonObject object) {
    String droneId = object.getString(Medication01.DRONE_ID);
    logger.info("newState: {}", object.getString("newState"));
    var newState  = Drone01.StateType.valueOf(object.getString("newState"));

    return droneRepository.updateState(droneId, newState)
        .compose(r -> Future.future(p -> p.complete(object)));
  }

  private Future<JsonObject> verifyDroneWeight(JsonObject object) {
    var drone = object.getJsonObject("drone");
    var medication = object.getJsonObject("medication");
    var totalWeight = object.getDouble("totalWeight");

    var expectedWeight = totalWeight + medication.getDouble(Medication01.WEIGHT);
    var droneWeightLimit = drone.getDouble(Drone01.WEIGHT_LIMIT);

   if(expectedWeight > droneWeightLimit) {
      return Future.failedFuture(new DroneException("drone.error.exceededLimit"));
    }

   if(expectedWeight == droneWeightLimit) {
     object.put("newState", Drone01.StateType.LOADED);
   }else if (expectedWeight < droneWeightLimit) {
      object.put("newState", Drone01.StateType.LOADING);
    }

    return Future.succeededFuture(object);
  }

  private Future<JsonObject> verifyDrone(JsonObject object) {
    var drone = object.getJsonObject("drone");
    var medication = object.getJsonObject("medication");

    var droneId = drone.getString(Drone01.ID);
    var medicationDroneId = medication.getString(Medication01.DRONE_ID);
    if(medicationDroneId != null && !medicationDroneId.equalsIgnoreCase(droneId)) {
      return Future.failedFuture(new DroneException("medication.error.already-loaded"));
    }

    if(droneId.equalsIgnoreCase(medicationDroneId)) {
      return Future.failedFuture(new DroneException("drone.error.already-loaded"));
    }

    var state = Drone01.StateType.valueOf(drone.getString(Drone01.STATE));
    if(forbidden.contains(state)) {
      return Future.failedFuture(new DroneException("drone.error.not-idle-loading"));
    }

    var batteryCapacity = drone.getDouble(Drone01.BATTERY_CAPACITY);
    if(batteryCapacity < 25) {
      return Future.failedFuture(new DroneException("drone.error.battery-tow-low"));
    }

    var totalWeigh = repository.totalDroneWeigh(drone.getString(Drone01.ID));

    return totalWeigh.compose(totalWeight -> Future.succeededFuture(object.put("totalWeight", totalWeight)));
  }
}
