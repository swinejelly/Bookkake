package models;

import java.util.*;
import com.avaje.ebean.*;

import play.Logger;
import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;

import javax.validation.constraints.*;
import javax.persistence.*;
import java.sql.Timestamp;

import controllers.Application;

@Entity
public class Book extends Model {
  @Id
  public Long id;

  @Size(min=3, max=128, message="Name must be between {min} and {max} characters!")
  public String name;

  @NotNull
  public Long userIdOwner;

  @NotNull
  public Long userIdPossessor;

  @javax.validation.constraints.Pattern(regexp="Using|Will lend|Want to Sell")
  public String status;

  @Size(min=0, max=128, message="Author names must be less than {max} characters!")
  public String author;

  @javax.validation.constraints.Pattern(regexp="True|False")
  public String active;//"True" or "False"

  @Future
  public Timestamp due;

  //an optional price for the user that will be displayed if the status is want to sell.
  @javax.validation.constraints.Min(0)
  @javax.validation.constraints.Max(100000)
  public Long targetPrice;
  

  public static Finder<Long, Book> find =
    new Finder(Long.class, Book.class);

  public static List<Book> search(String query){
    Logger.info("Search book request for query: " + query);
    List<Book> books = find.where().or(Expr.icontains("name", query), Expr.icontains("author", query)).findList();
    return books;
  }

  public static User getOwner(Book b){
    return User.find.where().eq("id", b.userIdOwner).findUnique();
  }

  public static User getPossessor(Book b){
    return User.find.where().eq("id", b.userIdPossessor).findUnique();
  }

  public static List<Book> getPossessedBooks(User u){
    return Book.find.where().eq("userIdPossessor", u.id).findList();
  }

  public static List<Book> getOwnedBooks(User u){
    return Book.find.where().eq("userIdOwner", u.id).findList();
  }

  public static List<Book> getOwnedAndPossessedBooks(User u){
    return Book.find.where().or(Expr.eq("userIdPossessor", u.id), Expr.eq("userIdOwner", u.id)).findList();
  }

  public boolean isActive(){
    return active.equalsIgnoreCase("True");
  }

  public String priceAsString(){
    if(targetPrice == null){
      return "";
    }else{
      return String.format("%.2f", targetPrice / 100.0);
    }
  }


}

