package controllers;

import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

    public static Result index() {
        Form<User> userForm = Form.form(User.class);

        return ok(index.render(userForm, User.find.all()));
    }

    public static Result addUser() {
        Form<User> userData = Form.form(User.class).bindFromRequest();
        User user = userData.get();
        user.save();

        return redirect("/");
    }

}
