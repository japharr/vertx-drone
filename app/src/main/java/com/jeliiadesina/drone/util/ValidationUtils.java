package com.jeliiadesina.drone.util;

import am.ik.yavi.core.ConstraintViolations;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationUtils {
  public static JsonArray extract(ConstraintViolations violations) {
    List<JsonObject> list = new ArrayList<>();
    violations.forEach(r -> list.add(
      new JsonObject()
        .put("message", r.message())
        .put("messageKey", r.messageKey())
        .put("field", r.name())
        .put("value", r.violatedValue()
        )));

    Map<String, List<JsonObject>> result = list.stream().collect(Collectors
      .groupingBy(item -> item.getString("field")));
    JsonArray array = new JsonArray();

    result.forEach((key, value) -> {
      JsonArray arr = new JsonArray();
      value.forEach(it -> {
        arr.add(new JsonObject()
          .put("message", it.getString("message"))
          .put("messageKey", it.getString("messageKey"))
          .put("value", it.getString("value"))
        );
      });

      array.add(new JsonObject()
        .put("field", key)
        .put("details", arr));
    });

    return array;
  }
}

