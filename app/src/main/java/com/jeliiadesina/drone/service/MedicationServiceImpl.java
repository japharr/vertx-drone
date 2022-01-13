package com.jeliiadesina.drone.service;

import com.jeliiadesina.drone.repository.MedicationRepository;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class MedicationServiceImpl implements MedicationService {
  private final MedicationRepository repository;

  public MedicationServiceImpl(MedicationRepository repository) {
    this.repository = repository;
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
