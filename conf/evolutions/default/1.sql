# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table auction_item (
  id                        number(19) not null,
  name                      varchar2(255),
  price                     number(38),
  constraint pk_auction_item primary key (id))
;

create sequence auction_item_seq;




# --- !Downs

drop table auction_item cascade constraints purge;

drop sequence auction_item_seq;

