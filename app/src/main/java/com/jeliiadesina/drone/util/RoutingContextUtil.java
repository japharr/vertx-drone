package com.jeliiadesina.drone.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RoutingContextUtil {
  public static JsonObject jsonBody(RoutingContext ctx) {
    if (ctx.getBody().length() == 0) {
      return new JsonObject();
    } else {
      return ctx.getBodyAsJson();
    }
  }

  public static String getLanguageKey(RoutingContext ctx, JsonObject i10nConf) {
    Optional<String> tag = ctx.acceptableLanguages().stream().map(LanguageHeader::tag).findFirst();
    Set<String> supportedLanguages = i10nConf.getJsonArray("tags")
        .stream().map(Object::toString).collect(Collectors.toSet());

    return tag.filter(supportedLanguages::contains).orElse("en");
  }
}
