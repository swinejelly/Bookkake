# --- !Ups

alter table book
add column is_library boolean default false;


# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table book 
remove column is_library;

SET FOREIGN_KEY_CHECKS=1;

