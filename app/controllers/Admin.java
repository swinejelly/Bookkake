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
 * Administrator Controller.
 */
public class Admin extends Controller {

	public static Result home(){
		return ok(admin.render());
	}

	public static Result library(){
		List<Book> books = Book.find.where().eq("isLibrary", true).eq("active", "True").findList();
		return ok(library.render(books));
	}

	public static Result libraryAdmin(){
		return ok(libraryAdmin.render());
	}



}
