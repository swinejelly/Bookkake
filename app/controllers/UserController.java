package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.*;
import views.html.*;

import models.*;

import java.util.*;
/**
 * User Controller.
 */
public class UserController extends Controller {

	/**
	 * Modifies the current user based on the information of the post
	 * request and redirects to the main index.
	 */
	public static Result editUser(){
		User u = Application.getUser();
		DynamicForm form = new DynamicForm().bindFromRequest();
		u.location = form.get("location");

		String errors = Application.errorsAsString(u);
		if (!errors.isEmpty()){
			flash("errors", errors);
		}else{
			u.update();
			flash("success", "Profile updated");
		}
		return redirect(routes.Application.displayUser(Application.getUser().id));
	}


}
