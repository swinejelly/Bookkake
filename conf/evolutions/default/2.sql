# --- !Ups

alter table user 
add (      real_name         varchar(255),
           location          varchar(255));



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table user 
drop column real_name, location;

SET FOREIGN_KEY_CHECKS=1;

