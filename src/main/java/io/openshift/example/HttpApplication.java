package io.openshift.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpApplication extends AbstractVerticle {

    private static final String template = "Hello, %s!";

    private boolean online = false;

    @Override
    public void start(Promise<Void> done) {
        Router router = Router.router(vertx);

        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx).register("server-online",
                fut -> fut.complete(online ? Status.OK() : Status.KO()));

        router.get("/api/greeting").handler(this::greeting);
        router.get("/api/stop").handler(this::stopTheService);
        router.get("/api/health/readiness").handler(rc -> rc.response().end("OK"));
        router.get("/api/health/liveness").handler(healthCheckHandler);
        router.get("/").handler(StaticHandler.create());

        vertx.createHttpServer().requestHandler(router).listen(config().getInteger("http.port", 8080), ar -> {
            online = ar.succeeded();
            done.handle(ar.mapEmpty());
        });
    }

    private void stopTheService(RoutingContext rc) {
        rc.response().end("Stopping HTTP server, Bye bye world !");
        online = false;
    }

    private void greeting(RoutingContext rc) {
        if (!online) {
            rc.response().setStatusCode(400).putHeader(CONTENT_TYPE, "text/plain").end("Not online");
            return;
        }
        String coronaresponse = "";
        String name = rc.request().getParam("name");
        if (name.equals("CoronaInfo")) {
            // TODO HttpRequest request = HttpRequest.newBuilder()
          
            HttpResponse<String> response;
            try {
                response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.body());
                coronaresponse = (String) response.body();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
    if (name == null) {
      name = "World";
    }

    JsonObject response = new JsonObject()
      .put("content", String.format(template, name + coronaresponse));

    rc.response()
      .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
      .end(response.encodePrettily());
  }
}
