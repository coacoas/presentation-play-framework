package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.hello;
import views.html.index;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result hello(String name, int age) {
        return ok(hello.render(name, age));
    }
}
