package com.jeliiadesina.drone.service;

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

}
