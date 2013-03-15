package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import play.data.validation.Constraints.*;
import views.html.*;

import models.*;

import java.util.*;
import java.sql.Timestamp;
import java.text.*;

public class TransactionController extends Controller {

  /*
   * Record the creation of a new book.
   */
  public static void bookCreation(User creator, Book b){
    Transaction t = new Transaction();
    t.userId = creator.id;
    t.bookId = b.id;
    t.kind = "Create";
    t.save();
  }

  /*
   * Record the deletion of a book.
   */
  public static void bookDeletion(User deletor, Book b){
    Transaction t = new Transaction();
    t.userId = deletor.id;
    t.bookId = b.id;
    t.kind = "Remove";
    t.save();
  }


  /*
   * Record the transfer of a book.
   */
  public static void bookTransfer(User giver, User receiver, 
		  		  Book b, Long price, Timestamp due){
    Transaction t = new Transaction();
    t.userId = giver.id;
    t.receiverId = receiver.id;
    t.bookId = b.id;
    t.payment = price;
    t.kind = "Transfer";
    //due date may be null in case of a permanent gift.
    t.dueDate = due;
    t.save();
  }

  public static void bookCheckout(User receiver, Book b){
    Transaction t = new Transaction();
    t.userId = receiver.id;
    t.bookId = b.id;
    t.kind = "Checkout";
    t.save();
  }
}
