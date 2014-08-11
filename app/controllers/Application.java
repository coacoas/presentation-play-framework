package controllers;

import models.User;
import play.data.DynamicForm;
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
        DynamicForm userData = Form.form().bindFromRequest();
        User user = new User();
        user.setFirstName(userData.get("firstName"));
        user.setLastName(userData.get("lastName"));
        user.setEmail(userData.get("email"));
        user.setPhone(userData.get("phone"));

        user.save();

        return redirect("/");
    }

}
