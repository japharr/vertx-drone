package com.jeliiadesina.drone.service;

import com.jeliiadesina.drone.entity.Drone01;
import com.jeliiadesina.drone.exception.AlreadyExistException;
import com.jeliiadesina.drone.exception.NotFoundException;
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
            if(rx.cause() instanceof AlreadyExistException ex) {
              msg.fail(ex.getCode(), ex.getMessage());
            } else {
              msg.fail(500, rx.cause().getMessage());
            }
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
  public void fetchById(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    String id = data.getString(Drone01.ID);
    droneRepository.findById(id)
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
  public void fetchBySerialNumber(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    String serialNumber = data.getString(Drone01.SERIAL_NUMBER);
    droneRepository.findDroneBySerialNumber(serialNumber)
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
  public void fetchDronesByState(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    Drone01.StateType state = Drone01.StateType.valueOf(data.getString(Drone01.STATE, "IDLE"));

    droneRepository.findDronesByState(state)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            msg.fail(501, rx.cause().getMessage());
          }
        });
  }

  @Override
  public void fetchAvailableDrones(Message<Object> msg) {
    droneRepository.findDAvailableDrones()
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            msg.fail(501, rx.cause().getMessage());
          }
        });
  }
}
