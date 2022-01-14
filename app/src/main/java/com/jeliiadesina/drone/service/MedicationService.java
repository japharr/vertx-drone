package com.jeliiadesina.drone.service;

import io.vertx.core.eventbus.Message;

public interface MedicationService {
  void create(Message<Object> msg);

  void updateImage(Message<Object> msg);

  void fetchByName(Message<Object> msg);

  void fetchByDrone(Message<Object> msg);

  void fetchAll(Message<Object> msg);
}
