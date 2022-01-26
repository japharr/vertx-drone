package com.jeliiadesina.drone.entity;

import io.vertx.core.json.JsonObject;

public interface Medication01 {
  // field-name
  String NAME = "name";
  String WEIGHT = "weight";
  String CODE = "code";
  String IMAGE = "image";
  String DRONE_ID = "droneId";

  // event-buss addresses
  String CREATE_ADDRESS = "medication.create";
  String FETCH_ALL_ADDRESS = "medication.fetch-all";
  String UPLOAD_IMAGE_ADDRESS = "medication.upload-image";
  String FETCH_BY_NAME_ADDRESS = "drone.fetch-name";
  String FETCH_BY_SERIAL_NUMBER_ADDRESS = "drone.fetch-drone-serialnumber";
  String ADD_MEDICATION_TO_DRONE_ADDRESS = "medication.add.to-drone";


  // construct Drone object
  static JsonObject object(JsonObject body) {
    return new JsonObject()
        .put(NAME, body.getString(NAME))
        .put(WEIGHT, body.getDouble(WEIGHT))
        .put(CODE, body.getString(CODE))
        .put(IMAGE, body.getString(IMAGE));
  }

  static JsonObject objectMedicationName(JsonObject body) {
    return new JsonObject()
        .put(NAME, body.getString(NAME));
  }

  // sql queries
  static String insertOneQuery() {
    return "INSERT INTO medications (uuid, name, weight, code, created_date, last_modified_date ) VALUES($1, $2, $3, $4, current_timestamp, current_timestamp)";
  }

  static String countByNameQuery() {
    return "SELECT count(*) FROM medications WHERE name = $1";
  }

  static String selectOneByName() {
    return "SELECT uuid as id, name, weight, code, image, drone_uuid as drone_id FROM medications " +
        "WHERE name = $1 LIMIT 1";
  }

  static String selectAllByDroneId() {
    return "SELECT uuid as id, name, weight, code, image, drone_uuid as drone_id FROM medications " +
        "WHERE drone_uuid = $1";
  }

  static String updateWithImage() {
    return "UPDATE medications SET image = $2, last_modified_date = current_timestamp " +
        "WHERE name = $1 RETURNING *";
  }

  static String updateWithDroneId() {
    return "UPDATE medications SET drone_uuid = $2, last_modified_date = current_timestamp " +
        "WHERE name = $1 RETURNING *";
  }

  static String selectTotalMedicationWeigh() {
    return "SELECT drone_uuid as drone_id, SUM(weight) as total_weight FROM medications " +
        "WHERE (drone_uuid = $1) GROUP BY drone_uuid";
  }

  static String selectAllQuery() {
    return "SELECT uuid as id, name, weight, code, image, drone_uuid as drone_id FROM medications";
  }
}