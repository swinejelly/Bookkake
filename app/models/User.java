package models;

import java.util.*;

import models.*;

import play.db.ebean.*;
import static play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;


import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
/**
 * Model to represent a unique user with a username. 
 */
public class User extends Model {
  @Id
  public Long id;

  public String userName;
  @Size(min=0, max=64, message="Location must be less than {max} characters!")
  public String location;
  public String realName;

  public static Finder<Long, User> find =
    new Finder(Long.class, User.class);

  public void giveMessage(String message){
    Message m = new Message();
    m.recipientId = id;
    m.contents = message;
    m.save();
  }
}

