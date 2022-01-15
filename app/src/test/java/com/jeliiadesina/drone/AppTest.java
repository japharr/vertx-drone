
package com.jeliiadesina.drone;

import com.jeliiadesina.drone.vertcle.DatabaseVerticle;
import com.jeliiadesina.drone.vertcle.WebVerticle;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@Testcontainers
public class AppTest {
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

    vertx.deployVerticle(new DatabaseVerticle(), options, vtx.succeeding(dvId -> {
      vertx.deployVerticle(new WebVerticle(), options, vtx.succeeding(wId -> {
        vtx.completeNow();
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

  @Test
  @Order(1)
  @DisplayName("Register some drones")
  void test_can_register_drones() {
    drones.forEach((key, registration) -> {
      given(requestSpecification)
          .contentType(ContentType.JSON)
          .body(registration.encode())
          .post("/api/v1/drones")
          .then()
          .assertThat()
          .statusCode(200);
    });
  }

  @Test
  @Order(2)
  @DisplayName("Fetch all drones")
  void test_can_fetch_register_drones() {
    JsonPath jsonPath = given()
        .spec(requestSpecification)
        .accept(ContentType.JSON)
        .get("/api/v1/drones")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .extract().jsonPath();

    List<Object> items = jsonPath.get("$");
    assertThat(items).isNotEmpty();
  }

  @Test
  @Order(3)
  @DisplayName("Fetch a drone")
  void test_can_fetch_a_drone() {
    JsonPath jsonPath = given()
        .spec(requestSpecification)
        .accept(ContentType.JSON)
        .get("/api/v1/drones/drone-01")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .extract().jsonPath();

    JsonObject droneO1 = drones.get("drone-01");
    List<String> props = asList("serialNumber", "model", "weightLimit", "batteryCapacity");
    props.forEach(prop -> assertThat(jsonPath.getString(prop)).isEqualTo(droneO1.getString(prop)));
    assertThat(jsonPath.getString("state")).isEqualTo("IDLE");
  }
}
