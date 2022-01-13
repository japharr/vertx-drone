package com.jeliiadesina.drone.service;

import io.vertx.core.eventbus.Message;

public interface DroneService {
  void registerDrone(Message<Object> msg);

  void fetchAllDrones(Message<Object> msg);

  void fetchById(Message<Object> msg);

  void fetchBySerialNumber(Message<Object> msg);

  void fetchDronesByState(Message<Object> msg);
}
