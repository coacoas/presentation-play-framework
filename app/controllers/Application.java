package controllers;

import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

public class Application extends Controller {

    public static F.Promise<Result> index() {
		final long start = System.currentTimeMillis();

		F.Function<WSResponse, Long> timing = (response) -> System.currentTimeMillis() - start;

        final F.Promise<Long> yahoo = WS.url("http://www.yahoo.com").get().map(timing);
		final F.Promise<Long> google = WS.url("http://www.google.com").get().map(timing);
		final F.Promise<Long> bing = WS.url("http://www.bing.com").get().map(timing);

		return F.Promise.sequence(yahoo, google, bing).map(timings -> {
					Map<String, Long> results = new HashMap<>();
					results.put("yahoo", yahoo.get(100));
					results.put("google", google.get(100));
					results.put("bing", bing.get(100));
					return ok(Json.toJson(results));
				});
    }

}
