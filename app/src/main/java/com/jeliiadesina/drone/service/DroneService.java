package com.jeliiadesina.drone.service;

import io.vertx.core.eventbus.Message;

public interface DroneService {
  void registerDrone(Message<Object> msg);
}
