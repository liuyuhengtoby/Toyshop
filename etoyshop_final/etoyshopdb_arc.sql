CREATE DATABASE IF NOT EXISTS etoyshop;

USE etoyshop;

DROP TABLE IF EXISTS toys;
CREATE TABLE toys (
  id       INT,
  title    VARCHAR(50),
  price    FLOAT,
  qty      INT,
  category VARCHAR(50),
  PRIMARY KEY (id));

INSERT INTO toys VALUES (1001, 'Tumbletuft Bunny', 28.88, 11, 'Animals');
INSERT INTO toys VALUES (1002, 'Tumbletuft Squirrel', 29.99, 22, 'Animals');
INSERT INTO toys VALUES (1003, 'Amuseable Snowflake', 33.33, 33, 'Decorations');
INSERT INTO toys VALUES (1004, 'Amuseable Potted Bamboo', 23.44, 44, 'Decorations');
INSERT INTO toys VALUES (1005, 'Sweater Sausage Dog Pink', 35.55, 55, 'Animals');
INSERT INTO toys VALUES (1006, 'Amuseable Cloud Bag', 36.66, 66, 'Decorations');
INSERT INTO toys VALUES (1007, 'Amuseable Slice Of Pizza', 37.77, 17, 'Food');

DROP TABLE IF EXISTS users;

create table users (
    id int not null auto_increment,
    user_name varchar(10),
    pw varchar(16),
    phone varchar(8),
    email varchar(20),
    addr varchar(50),
    primary key (id)
);

DROP TABLE IF EXISTS orders;

create table orders (
    id int not null auto_increment,
    toy_id int not null,
    qty int,
    user_id int,
    primary key (id)
);