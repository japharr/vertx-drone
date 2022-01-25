package com.jeliiadesina.drone.config;

import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;

public abstract class PgConfig {
  public static final String DB = "db";
  public static final String POOL = "pool";
  public static final String MAX_SIZE = "max-size";

  public static final String HOST = "host";
  public static final String PORT = "port";
  public static final String DATABASE = "database";
  public static final String USER = "user";
  public static final String PASSWORD = "password";

  public static PgConnectOptions pgConnectOpts(JsonObject config) {
    JsonObject db = config.getJsonObject(DB, new JsonObject());

    return new PgConnectOptions()
      .setPort(db.getInteger(PORT))
      .setHost(db.getString(HOST))
      .setDatabase(db.getString(DATABASE))
      .setUser(db.getString(USER))
      .setPassword(db.getString(PASSWORD));
  }

  public static PoolOptions poolOptions(JsonObject config) {
    JsonObject db = config.getJsonObject(DB, new JsonObject());
    JsonObject pool = db.getJsonObject(POOL, new JsonObject());

    return new PoolOptions().setMaxSize(pool.getInteger(MAX_SIZE));
  }

  public static String resolveUrl(JsonObject db) {
    String host = db.getString(HOST);
    int port = db.getInteger(PORT);
    String database = db.getString("database");

    return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
  }
}
