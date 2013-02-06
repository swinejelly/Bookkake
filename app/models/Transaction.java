package models;

import java.util.*;

import play.Logger;
import play.db.ebean.*;
import play.data.validation.Constraints.*;
import play.db.ebean.Model.Finder;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class Transaction extends Model {
  @Id
  public Long id;

  @Required
  public Long userId;

  public Long receiverId;

  @Required
  public Long bookId;

  @Min(0)
  public Long payment;//in cents US

  @Required
  public String kind;//can be Create, Transfer, Loan, Remove

  @Required
  public Timestamp date;//Date of the transaction

  public Timestamp dueDate;//Date when a loan needs to be returned

  public Transaction(){
    date = new Timestamp(System.currentTimeMillis());
  }

  public static Finder<Long, Transaction> find =
    new Finder(Long.class, Transaction.class);
}
