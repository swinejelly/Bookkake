# --- !Ups

alter table book
add column target_price bigint;


# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table book 
drop column target_price;

SET FOREIGN_KEY_CHECKS=1;

