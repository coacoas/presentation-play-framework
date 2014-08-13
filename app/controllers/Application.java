package controllers;

import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.HashMap;
import java.util.Map;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static F.Promise<Result> timing() {
        long start = System.currentTimeMillis();
        F.Function<WSResponse, Long> timing = response -> System.currentTimeMillis() - start;

        F.Promise<Long> yahoo = WS.url("http://www.yahoo.com").get().map(timing);
        F.Promise<Long> google = WS.url("http://www.google.com").get().map(timing);
        F.Promise<Long> bing = WS.url("http://www.bing.com").get().map(timing);

        return F.Promise.sequence(yahoo, google, bing).map(timings -> {
                    Map<String, Long> map = new HashMap<>();
                    map.put("yahoo", yahoo.get(10));
                    map.put("google", google.get(10));
                    map.put("bing", bing.get(10));
                    map.put("total", System.currentTimeMillis() -start);

                    return ok(Json.toJson(map));
                }
        );
    }

}
