# --- !Ups

create table book (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  user_id_owner             bigint,
  user_id_possessor         bigint,
  status                    varchar(255),
  author                    varchar(255),
  active                    varchar(255),
  due                       datetime,
  constraint pk_book primary key (id))
;

create table message (
  id                        bigint auto_increment not null,
  recipient_id              bigint,
  contents                  varchar(255),
  constraint pk_message primary key (id))
;

create table transaction (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  receiver_id               bigint,
  book_id                   bigint,
  payment                   bigint,
  kind                      varchar(255),
  date                      datetime,
  due_date                  datetime,
  constraint pk_transaction primary key (id))
;

create table user (
  id                        bigint auto_increment not null,
  user_name                 varchar(255),
  constraint pk_user primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table book;

drop table message;

drop table transaction;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

