package com.jeliiadesina.drone;

import com.jeliiadesina.drone.api.WebVerticle;
import com.jeliiadesina.drone.database.DatabaseVerticle;
import com.jeliiadesina.drone.migration.MigrationVerticle;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class MainVerticleTest {
  private static RequestSpecification requestSpecification;

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13.4")
    .withDatabaseName("test").withUsername("test").withPassword("test");

  @BeforeAll
  static void prepareSpec() {
    requestSpecification = new RequestSpecBuilder()
      .addFilters(asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))
      .setBaseUri("http://localhost:8084/")
      .build();
  }

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext vtx) {
    JsonObject conf = new JsonObject()
      .put("http", new JsonObject().put("port", 8084).put("context-path", "/api/v1"))
      .put("db", new JsonObject().put("host", postgres.getContainerIpAddress()).put("port", postgres.getMappedPort(5432))
        .put("database", "test").put("user", "test").put("password", "test"))
      .put("i18n", new JsonObject().put("tags", new JsonArray().add("en").add("fr")));

    DeploymentOptions options = new DeploymentOptions().setConfig(conf);

    vertx.deployVerticle(new MigrationVerticle(), options, vtx.succeeding(mId -> {
      vertx.deployVerticle(new DatabaseVerticle(), options, vtx.succeeding(dId -> {
        vertx.deployVerticle(new WebVerticle(), options, vtx.succeeding(wId -> {
          vtx.completeNow();
        }));
      }));
    }));
  }

  private final Map<String, JsonObject> drones = Map.of(
    "drone-01", new JsonObject()
      .put("serialNumber", "drone-01")
      .put("model", "Middleweight")
      .put("weightLimit", 20.0)
      .put("batteryCapacity", 10.0),

    "drone-02", new JsonObject()
      .put("serialNumber", "drone-02")
      .put("model", "Cruiserweight")
      .put("weightLimit", 40.0)
      .put("batteryCapacity", 60.0)
  );

  private final Map<String, JsonObject> medications = Map.of(
    "medication-01", new JsonObject()
      .put("name", "medication-01")
      .put("weight", 30.0)
      .put("code", "MED01"),

    "medication-02", new JsonObject()
      .put("name", "medication-02")
      .put("weight", 20.0)
      .put("code", "MED02")
  );

  @Test
  @Order(1)
  @DisplayName("Register some drones")
  void test_can_register_drones() {
    drones.forEach((key, registration) -> {
      given(requestSpecification)
        .contentType(ContentType.JSON)
        .body(registration.encode())
        .post("/drones")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK);
    });
  }
}
