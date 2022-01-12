package com.jeliiadesina.drone.vertcle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;

public class WebVerticle extends AbstractVerticle {
  private final JsonObject httpConf = new JsonObject();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    httpConf.mergeIn(config().getJsonObject("http"));

    startWebApp()
        .onComplete(http ->
            completeStartup(http, startPromise));
  }

  private void completeStartup(AsyncResult<HttpServer> http, Promise<Void> startPromise) {
    if (http.succeeded()) {
      startPromise.complete();
    } else {
      startPromise.fail(http.cause());
    }
  }

  private Future<HttpServer> startWebApp() {
    Router router = Router.router(vertx);

    BodyHandler bodyHandler = BodyHandler.create();
    router.post().handler(bodyHandler);
    router.put().handler(bodyHandler);

    var basePath = httpConf.getString("context-path", "/api/v1");
    var port = httpConf.getInteger("port", 8080);

    return Future.future(promise -> vertx.createHttpServer()
        .requestHandler(router)
        .listen(port, promise));
  }
}
