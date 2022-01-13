package com.jeliiadesina.drone.service;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.repository.DroneRepository;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class DroneServiceImpl implements DroneService {
  private final DroneRepository droneRepository;

  public DroneServiceImpl(DroneRepository droneRepository) {
    this.droneRepository = droneRepository;
  }

  @Override
  public void registerDrone(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    droneRepository.persistDrone(data)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(data);
          } else {
            msg.fail(500, rx.cause().getLocalizedMessage());
          }
        });
  }

  @Override
  public void fetchAllDrones(Message<Object> msg) {
    droneRepository.findAllDrones()
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            msg.fail(501, rx.cause().getMessage());
          }
        });
  }

  @Override
  public void fetchDronesByState(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    Drone.StateType state = Drone.StateType.valueOf(data.getString(Drone.STATE, "IDLE"));

    droneRepository.findDronesByState(state)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            msg.fail(501, rx.cause().getMessage());
          }
        });
  }
}
