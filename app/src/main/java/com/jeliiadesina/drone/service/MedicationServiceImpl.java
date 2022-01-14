package com.jeliiadesina.drone.service;

import com.jeliiadesina.drone.entity.Drone;
import com.jeliiadesina.drone.entity.Medication;
import com.jeliiadesina.drone.repository.DroneRepository;
import com.jeliiadesina.drone.repository.MedicationRepository;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class MedicationServiceImpl implements MedicationService {
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
            msg.fail(500, rx.cause().getLocalizedMessage());
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

    String name = data.getString(Medication.NAME);
    repository.findByName(name)
        .onComplete(rx -> {
          if(rx.succeeded()) {
            msg.reply(rx.result());
          } else {
            msg.fail(501, rx.cause().getMessage());
          }
        });
  }

  @Override
  public void fetchByDrone(Message<Object> msg) {
    if(!(msg.body() instanceof JsonObject data)) {
      msg.fail(400, "json not formatted");
      return;
    }

    String serialNumber = data.getString(Drone.SERIAL_NUMBER);
    droneRepository.findDroneBySerialNumber(serialNumber)
        .compose(json -> droneRepository.findById(json.getString(Drone.ID)))
        .onComplete(rx -> {
            if(rx.succeeded()) {
                msg.reply(rx.result());
            } else {
                msg.fail(501, rx.cause().getMessage());
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
}
