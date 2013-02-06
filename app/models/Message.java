package models;

import java.util.*;

import models.*;

import play.db.ebean.*;
import play.data.validation.Constraints.*;

import static play.db.ebean.Model.Finder;
import javax.persistence.*;

@Entity
/**
 * Model to represent a message for a specific user
 */
public class Message extends Model {
  @Id
  public Long id;

  @Required
  public Long recipientId;

  @Required
  public String contents;

  public static Finder<Long, Message> find =
    new Finder(Long.class, Message.class);
}

