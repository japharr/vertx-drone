package com.jeliiadesina.drone.api.handler;

import com.jeliiadesina.drone.api.model.MedDto;
import com.jeliiadesina.drone.database.service.DroneDatabaseService;
import com.jeliiadesina.drone.database.service.MedicationDatabaseService;
import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import com.jeliiadesina.drone.entity.enumeration.State;
import com.jeliiadesina.drone.exception.DroneException;
import com.jeliiadesina.drone.exception.ResourceNotFoundException;
import com.jeliiadesina.drone.util.Pair;
import com.jeliiadesina.drone.util.Triple;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
          var drone = pair.first();
          var medication = pair.second();
          var totalWeigh = medicationDatabaseService.totalDroneWeigh(drone.getId());
          return totalWeigh
            .compose(weight -> verifyWeigh(pair, weight))
            .compose(state -> Future.succeededFuture(new Triple<>(state, drone, medication)));
        })
        .compose(triple -> {
          var drone = triple.second();
          var medication = triple.third();
          var state = triple.first();

          var updateFuture = medicationDatabaseService.updateMedicationWithDrone(medication.getId(), drone.getId());
          var updateState = droneDatabaseService.updateState(drone.getId(), state);
          return updateFuture.compose(res -> updateState);
        })
        .onComplete(res -> {
          if (res.succeeded()) {
            restResponse(ctx, 200);
          } else {
            if (res.cause() instanceof DroneException) {
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

    if (expectedWeight > droneWeightLimit) {
      return Future.failedFuture(new DroneException("drone.error.exceededLimit"));
    }

    var state = State.LOADING;
    if (expectedWeight == droneWeightLimit) {
      state = State.LOADED;
    }

    return Future.succeededFuture(state);
  }

  public static Handler<RoutingContext> getAll(MedicationDatabaseService medicationDatabaseService, DroneDatabaseService droneDatabaseService) {
    return ctx -> {
      droneDatabaseService.findAll()
        .compose(items -> fetchCurrentWeight(items, medicationDatabaseService))
        .onComplete((res -> {
          if (res.succeeded()) {
            JsonArray array = new JsonArray();
            res.result().forEach(item -> {
              var drone = (JsonObject) item;
              array.add(drone);
            });
            restResponse(ctx, 200, array.encode());
          } else {
            ctx.fail(res.cause());
          }
        }));
    };
  }

  public static Handler<RoutingContext> getAvailable(MedicationDatabaseService medicationDatabaseService, DroneDatabaseService droneDatabaseService) {
    return ctx -> {
      droneDatabaseService.findAllAvailable()
        .compose(items -> fetchCurrentWeight(items, medicationDatabaseService))
        .onComplete((res -> {
          if (res.succeeded()) {
            JsonArray array = new JsonArray();
            res.result().forEach(item -> {
              var drone = (Drone) item;
              array.add(new JsonObject(drone.toString()));
            });
            restResponse(ctx, 200, array.encode());
          } else {
            ctx.fail(res.cause());
          }
        }));
    };
  }

  private static Future<List> fetchCurrentWeight(List<Drone> drones, MedicationDatabaseService medicationDatabaseService) {
    List<Future> futures = new ArrayList<>();
    drones.forEach(drone -> {
      Future<Double> weightFuture = medicationDatabaseService.totalDroneWeigh(drone.getId());
      Future<Drone> droneFuture = weightFuture.compose(weight -> {
        drone.setCurrentWeight(weight);
        return Future.succeededFuture(drone);
      });
      futures.add(droneFuture);
    });

    return CompositeFuture.all(futures)
      .compose(res -> Future.succeededFuture(res.list()));
  }

  private static Future<List> fetchCurrentWeight(JsonArray drones, MedicationDatabaseService medicationDatabaseService) {
    List<Future> futures = new ArrayList<>();
    drones.forEach(item -> {
      var drone = (JsonObject) item;
      Future<Double> weightFuture = medicationDatabaseService.totalDroneWeigh(drone.getString("id"));
      Future<JsonObject> droneFuture = weightFuture.compose(weight -> {
        drone.put("currentWeight", weight);
        return Future.succeededFuture(drone);
      });
      futures.add(droneFuture);
    });

    return CompositeFuture.all(futures)
      .compose(res -> Future.succeededFuture(res.list()));
  }

  public static Handler<RoutingContext> imageUpload(MedicationDatabaseService medicationDatabaseService) {
    return ctx -> {
      String name = ctx.pathParam("name");

      Optional<FileUpload> opt = ctx.fileUploads().stream().findFirst();
      if(!opt.isPresent() || !opt.get().contentType().contains("image")) {
        ctx.fail(new DroneException("Please, upload an image"));
        return;
      }

      FileUpload fileUpload = opt.get();
      medicationDatabaseService.findByName(name)
              .compose(med -> medicationDatabaseService
                  .updateMedicationWithImage(med.getId(), fileUpload.uploadedFileName()))
          .onComplete(rs -> {
            if(rs.succeeded()) {
              restResponse(ctx, 200, "Image uploaded successfully");
            } else {
              ctx.fail(rs.cause());
            }
          });
    };
  }
}
