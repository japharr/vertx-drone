package com.jeliiadesina.drone.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.RoutingContext;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public abstract class LocaleMessageUtil {
  public static final Set<String> SUPPORTED_I18N = Set.of("en", "fr");

  public static String getMessage(String lang, String key) {
    Locale locale = Locale.forLanguageTag(lang);

    if(locale == null) locale = Locale.ENGLISH;

    ResourceBundle labels = ResourceBundle.getBundle("messages", locale);

    return labels.getString(key);
  }

  public static String getMessage(String lang, String key, Object... input) {
    return MessageFormat.format(getMessage(lang, key), input);
  }
}
