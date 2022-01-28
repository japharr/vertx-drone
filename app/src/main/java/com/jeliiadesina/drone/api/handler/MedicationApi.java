package com.jeliiadesina.drone.api.handler;

import com.jeliiadesina.drone.api.model.MedDto;
import com.jeliiadesina.drone.database.service.DroneDatabaseService;
import com.jeliiadesina.drone.database.service.MedicationDatabaseService;
import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Drone01;
import com.jeliiadesina.drone.entity.Medication;
import com.jeliiadesina.drone.entity.Medication01;
import com.jeliiadesina.drone.entity.enumeration.State;
import com.jeliiadesina.drone.exception.DroneException;
import com.jeliiadesina.drone.exception.ResourceNotFoundException;
import com.jeliiadesina.drone.util.Pair;
import com.jeliiadesina.drone.util.Triple;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

import static com.jeliiadesina.drone.util.RestApiUtil.decodeBodyToObject;
import static com.jeliiadesina.drone.util.RestApiUtil.restResponse;

public class MedicationApi {
  private static final Set<State> FORBIDDEN = Set.of(State.DELIVERED, State.DELIVERING, State.RETURNING);

  public static Handler<RoutingContext> getAll(MedicationDatabaseService medicationDatabaseService) {
    return ctx -> {
      medicationDatabaseService.findAll()
          .onComplete(res -> {
            if (res.succeeded()) {
              restResponse(ctx, 200, res.result().encode());
            } else {
              restResponse(ctx, 500, res.cause().getMessage());
            }
          });
    };
  }

  public static Handler<RoutingContext> getByName(MedicationDatabaseService medicationDatabaseService) {
    return ctx -> {
      String name = ctx.pathParam("name");

      medicationDatabaseService.findByName(name)
          .onComplete(res -> {
            if (res.succeeded()) {
              restResponse(ctx, 200, res.result().toString());
            } else {
              restResponse(ctx, 500, res.cause().getMessage());
            }
          });
    };
  }

  public static Handler<RoutingContext> create(MedicationDatabaseService medicationDatabaseService) {
    return ctx -> {
      Medication medication = decodeBodyToObject(ctx, Medication.class);

      medicationDatabaseService.persist(medication)
          .onComplete(res -> {
            if (res.succeeded()) {
              restResponse(ctx, 200);
            } else {
              restResponse(ctx, 500, res.cause().getMessage());
            }
          });
    };
  }

  public static Handler<RoutingContext> getByDroneSerialNumber(MedicationDatabaseService medicationDatabaseService, DroneDatabaseService droneDatabaseService) {
    return ctx -> {
      String serialNumber = ctx.pathParam("serialNumber");

      droneDatabaseService.findBySerialNumber(serialNumber)
          .compose(drone -> medicationDatabaseService.findAllByDroneId(drone.getId()))
          .onComplete(res -> {
            if (res.succeeded()) {
              restResponse(ctx, 200, res.result().encode());
            } else {
              restResponse(ctx, 500, res.cause().getMessage());
            }
          });
    };
  }

  public static Handler<RoutingContext> addMedicationToDrone(MedicationDatabaseService medicationDatabaseService, DroneDatabaseService droneDatabaseService) {
    return ctx -> {
      String serialNumber = ctx.pathParam("serialNumber");

      MedDto medDto = decodeBodyToObject(ctx, MedDto.class);

      Future<Drone> droneFuture = droneDatabaseService.findBySerialNumber(serialNumber);
      Future<Medication> medicationFuture = medicationDatabaseService.findByName(medDto.name());

      CompositeFuture.all(droneFuture, medicationFuture)
          .compose(res -> verifyEntity(res.resultAt(0), res.resultAt(1)))
          .compose(pair -> {
            var drone = pair.first(); var medication = pair.second();
            var totalWeigh = medicationDatabaseService.totalDroneWeigh(drone.getId());
            return totalWeigh
                .compose(weight -> verifyWeigh(pair, weight))
                .compose(state -> Future.succeededFuture(new Triple<>(state, drone, medication)));
          })
          .compose(triple -> {
            var drone = triple.second(); var medication = triple.third();
            var state = triple.first();

            var updateFuture =  medicationDatabaseService.updateMedicationWithDrone(medication.getId(), drone.getId());
            var updateState = droneDatabaseService.updateState(drone.getId(), state);
            return updateFuture.compose(res -> updateState);
          })
          .onComplete(res -> {
            if (res.succeeded()) {
              restResponse(ctx, 200);
            } else {
              if(res.cause() instanceof DroneException) {
                ctx.fail(new DroneException(res.cause().getMessage()));
              } else ctx.fail(res.cause());
            }
          });
    };
  }

  private static Future<Pair<Drone, Medication>> verifyEntity(Drone drone, Medication medication) {
    if (drone == null) return Future.failedFuture(new ResourceNotFoundException("drone not found"));
    if (medication == null) return Future.failedFuture(new ResourceNotFoundException("medication not found"));

    var droneId = drone.getId();
    var medicationDroneId = medication.getDroneId();

    if (medicationDroneId != null && !medicationDroneId.equalsIgnoreCase(droneId)) {
      return Future.failedFuture(new DroneException("medication.error.already-loaded"));
    }

    if (droneId.equalsIgnoreCase(medicationDroneId)) {
      return Future.failedFuture(new DroneException("drone.error.already-loaded"));
    }

    var state = drone.getState();
    if (FORBIDDEN.contains(state)) {
      return Future.failedFuture(new DroneException("drone.error.not-idle-loading"));
    }

    var batteryCapacity = drone.getBatteryCapacity();
    if (batteryCapacity < 25) {
      return Future.failedFuture(new DroneException("drone.error.battery-tow-low"));
    }

    return Future.succeededFuture(new Pair<>(drone, medication));
  }

  private static Future<State> verifyWeigh(Pair<Drone, Medication> pair, double totalWeight) {
    var drone = pair.first();
    var medication = pair.second();

    var expectedWeight = totalWeight + medication.getWeight();
    var droneWeightLimit = drone.getWeightLimit();

    if(expectedWeight > droneWeightLimit) {
      return Future.failedFuture(new DroneException("drone.error.exceededLimit"));
    }

    var state = State.LOADING;
    if(expectedWeight == droneWeightLimit) {
      state = State.LOADED;
    }

    return Future.succeededFuture(state);
  }
}
