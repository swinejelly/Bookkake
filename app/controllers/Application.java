package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.validation.Constraints.*;
import play.data.validation.Validation;
import views.html.*;

import models.*;

import static java.lang.String.format;
import java.util.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.sql.Timestamp;
import java.text.*;
/**
 * Main Application controller.
 * All requests are handled by this class. Also provides basic 
 * global helper methods such as getUser() to the rest of the application.
 */
public class Application extends Controller {
  //Developer tool to allow him/her to act as another user
  //e.g. "su"
  private static String userOverride = null;

  /**
   * Action
   * Display the main index.
   */
  public static Result index() {
    return ok(index.render());
  }

  /**
   * Action
   * Display faq page
   */
  public static Result faq() {
    return ok(faq.render());
  }

  /**
   * Switch the currently running user. Should only be used in development mode.
   * Remove before deployment
   */
  public static Result switchUser(){
    if (!Play.isProd()){
      String user = new DynamicForm().bindFromRequest().get("user");
      userOverride = user.isEmpty() ? null : user;
      return redirect(routes.Application.index());
    }
    return redirectErr("That feature is disabled.");
  }

  /**
   * Add a new book to the database and record its creation.
   * This method must be called from an HTTP POST request with
   * the appropriate information.
   * name: book name
   * status: Want to Sell, Willing to Lend, or Using
   * author: optional: author(s)' names.
   * library: optional: boolean string indicating if this should be a library book (default false)
   */
  public static Result newBook(){
    DynamicForm form = new DynamicForm().bindFromRequest();
    boolean isLibrary = (form.get("library") == null) ? false : form.get("library").equalsIgnoreCase("true");
    //Build and save book
    Book b = new Book();
    b.isLibrary = isLibrary;
    b.name = form.get("name");
    if (isLibrary){
      b.userIdOwner = -1L;
      b.userIdPossessor = -1L;
    }else{
      Long currentUserId = getUser().id;
      b.userIdOwner = currentUserId;
      b.userIdPossessor = currentUserId;
    }
    b.status = isLibrary ? "In Library" : form.get("status");
    b.author = form.get("author");
    b.active = "True";
    Long cents = priceAsCents(form.get("price"));
    b.targetPrice = cents == null ? 0L : cents;
    if (errorsAsString(b).isEmpty()){
      b.save();
      //Create transaction
      Logger.info("Book "+b.name+" created.");
      TransactionController.bookCreation(getUser(), b);
      //Display success
      return redirectSucc("Book "+b.name+" created.");
    }else{
      return redirectErr(errorsAsString(b));
    }
  }

  public static Result displayBook(Long id){
    Book b = Book.find.byId(id);
    if (b != null){
      List<Transaction> transactions = Transaction.find.where().eq("bookId", b.id).orderBy("date ASC").findList();
      return ok(
        views.html.bookrender.render(b, transactions)
      );
    }else{
      return redirectErr("No book by id "+id.toString()+" exists");
    }
  }

  public static Result deleteBook(Long id){
    Book b = Book.find.byId(id);
    if (b != null && getUser().id == b.userIdOwner){
      //make the book inactive. The database will keep the records but it will 
      //be inaccessible on the website.
      b.active = "False";
      b.update();
      Logger.info("Book "+b.name+" set to inactive.");
      TransactionController.bookDeletion(getUser(), b);
      //Display success
      return redirectSucc("Book deleted.");
    }else{
      return redirectErr("No book by id "+id.toString()+" exists");
    }
  }

  public static Result books(String query){
    List<Book> books = Book.search(query);
    return ok(
      views.html.books.render(books)
    );
  }

  /**
   * Returns the user name, creating that user if not found in database.
   * username should be forwarded in http header as X-WEBAUTH-USER
   */
  public static User getUser(){
    String name = userOverride == null ? request().getHeader("X-WEBAUTH-USER") : userOverride;
    String realName = request().getHeader("X-WEBAUTH-LDAP-CN");
    User user = User.find.where().eq("userName", name).findUnique();
    if (user == null){ //if not found
      user = new User();
      user.userName = name;
      if (userOverride == null){
        user.realName = realName;
      }
      user.save();
      user.giveMessage(
        "Welcome to Bookkake!"
      );
      Logger.info("User "+name+" created.");
      return user;
    }else if (!realName.equals(user.realName)){
      user.realName = realName;
      user.update();
      return user;
    }else{
      return user;
    }
  }

  public static Result give(Long id){
    DynamicForm form = new DynamicForm().bindFromRequest();
    Book b = Book.find.byId(id);
    User u = User.find.where().eq("userName", form.get("name")).findUnique();
    Long cents = priceAsCents(form.get("price"));
    cents = cents == null ? 0L : cents;
    if (u != null && b != null && 
        b.userIdOwner == getUser().id && b.userIdPossessor == getUser().id){
      //update book's possession and ownership
      b.userIdPossessor = u.id;
      b.userIdOwner = u.id;
      b.update();
      //record that this book was given
      TransactionController.bookTransfer(getUser(), u, b, cents, null);
      //Display success
      return redirectSucc("Book given to "+u.userName);
    }else if (b == null){
      return redirectErr("No book by id "+id.toString()+" exists");
    }else{
      return redirectErr("You do not have permission to transfer that book.");
    }
  }


  public static Result loan(Long id){
    DynamicForm form = new DynamicForm().bindFromRequest();
    Book b = Book.find.byId(id);
    User u = User.find.where().eq("userName", form.get("name")).findUnique();
    Long cents = priceAsCents(form.get("price"));
    cents = cents == null ? 0L : cents;
    if (u != null && b != null && 
        b.userIdOwner == getUser().id && b.userIdPossessor == getUser().id){
      b.userIdPossessor = u.id;
      //create duedate
      String date = form.get("due-date");
      Timestamp dueDate; 
      try{
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        dueDate = new Timestamp(format.parse(date).getTime());
      }catch (ParseException e){
        return redirectErr(format("Due date %s could not be understood", date));
      }

      b.due = dueDate;
      if (dueDate.compareTo(new Date()) < 0){
        return redirectErr("Entered due-date is before present date; transaction canceled");
      }
      TransactionController.bookTransfer(getUser(), u, b, cents, dueDate);
      b.update();
      //Display success.
      return redirectSucc(format("Book loaned to %1$s; due on %2$s",
        u.userName,
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(dueDate)));

    //error cases
    }else if (u == null){
      return redirectErr("No user by that name found");
    }else if (b.userIdPossessor != getUser().id){
      return redirectErr("That book is already lent out to "+u.userName);
    }else{
      return redirectErr("Undefined error.");
    }
  }
    

  public static Result users(){
    return ok(
      views.html.users.render(User.find.all())
    );
  }

  /**
   * Action
   * Display the user with the given id
   */
  public static Result displayUser(Long id){
    User u = User.find.byId(id);
    if (u != null){
      return ok(
        views.html.displayuser.render(u)
      );
    }else{
      return redirectErr("No user by id "+id.toString()+" exists");
    }
  }

  public static Result returnBook(Long id){
    Book b = Book.find.byId(id);
    if (b != null && getUser().id == b.userIdOwner){
      b.userIdPossessor = b.userIdOwner;
      b.due = null;
      b.update();
      //create the transaction
      Transaction t = new Transaction();
      t.userId = b.userIdOwner;
      t.receiverId = b.userIdOwner;
      t.bookId = b.id;
      t.kind = "Return";
      t.save();
      //Display success
      return redirectSucc("Book returned");
    }else if (b == null){
      return redirectErr("No book by id "+id.toString()+" exists");
    }else{
      return redirectErr("The book "+b.name+" does not belong to you");
    }
  }

  public static Result editBook(Long id){
    Book b = Book.find.byId(id);
    if (b == null){
      return redirectErr("No book by id "+id.toString()+" exists");
    }else if (b.userIdOwner != getUser().id){
      return redirectErr("The book "+b.name+" does not belong to you");
    }else{
      DynamicForm form = new DynamicForm().bindFromRequest();
      b.name = form.get("name");
      b.author = form.get("author");
      b.status = form.get("status");
      if (b.status.equals("Want to Sell")){
        try{
          Double d = Double.parseDouble(form.get("price"));
          b.targetPrice = Math.round(d * 100);
	  if (b.targetPrice < 0L) b.targetPrice = 0L;
        } catch (NumberFormatException e){
          b.targetPrice = 0L;
        }
      }else{
        b.targetPrice = 0L;
      }
      if (errorsAsString(b).isEmpty()){
        b.update();
	return redirectSucc("Book "+b.name+" updated.");
      }else{
	return redirectErr(errorsAsString(b));
      }
    }
  }

  /*
   * Redirect to root index with error message
   */
  public static Result redirectErr(String err){
    flash("errors", err);
    return redirect(routes.Application.index()); 
  }

  /*
   * Redirect to root index with success message
   */
  public static Result redirectSucc(String succ){
    flash("success", succ);
    return redirect(routes.Application.index()); 
  }

  /*
   * Return a set of strings of a given objects Validation errors.
   */
  public static Set<String> errors(Object o){
    Validator validator = Validation.getValidator();
    Set<ConstraintViolation<Object>> violations = validator.validate(o);
    Set<String> errSet = new HashSet<String>();
    for (ConstraintViolation<Object> cv: violations){
      errSet.add(cv.getMessage());
    }
    return errSet;
  }

  /*
   * Returns an objects errors as a string delimited by semicolons
   * If no errors, returns an empty string.
   */
  public static String errorsAsString(Object o){
    Set<String> errors = errors(o);
    StringBuilder sb = new StringBuilder();
    for (String err: errors){
      sb.append(err);
      sb.append(';');
    }
    if (sb.length() > 0){
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  /**
   * Parse the price as cents, and return it as a Long.
   * Return null if input is invalid.
   */
  private static Long priceAsCents(String str){
    Long cents = null;
    try{
      if (str != null){
        Double price = Double.parseDouble(str);
        cents = Math.round(price * 100);
	if (cents < 0) cents = null;
      }
    }catch (NumberFormatException e){
      return null;
    }
    return cents;
  }
}
