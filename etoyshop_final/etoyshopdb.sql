
CREATE DATABASE IF NOT EXISTS etoyshop;

USE etoyshop;

DROP TABLE IF EXISTS toys;
CREATE TABLE toys (
  id       INT,
  title    VARCHAR(50),
  price    FLOAT,
  qty      INT,
  category VARCHAR(50),
  image_url VARCHAR(255),
  PRIMARY KEY (id)
);

INSERT INTO toys (id, title, price, qty, category, image_url) VALUES 
(1001, 'Tumbletuft Bunny', 10.11, 11, 'Animals', 'jellycatpic\\bunny.jpg'),
(1002, 'Tumbletuft Squirrel', 20.22, 22, 'Animals', 'jellycatpic\\squirrel.webp'),
(1003, 'Amuseable Snowflake', 30.33, 33, 'Decorations', 'jellycatpic\\snowflake.webp'),
(1004, 'Amuseable Potted Bamboo', 40.44, 44, 'Decorations', 'jellycatpic\\bamboo.webp'),
(1005, 'Winter Warmer Pippa Black Labrador', 50.55, 55, 'Animals', 'jellycatpic\\dog.webp'),
(1006, 'Amuseable Cloud Bag', 60.66, 66, 'Decorations', 'jellycatpic\\could2.jpeg'),
(1007, 'Amuseable Slice Of Pizza', 70.77, 17, 'Food', 'jellycatpic\\pizza.webp'),
(1008, 'Amuseable Avocado', 80.88, 8, 'Food', 'jellycatpic\\avocado.jpg'),
(1009, 'Amuseable Ramen', 90.99, 29, 'Food', 'jellycatpic\\Raman.webp'),
(1010, 'Big Spottie Elephant', 100.00, 1, 'Animals', 'jellycatpic\\elephant.webp');


DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id INT NOT NULL AUTO_INCREMENT,
  user_name VARCHAR(10),
  pw VARCHAR(16),
  phone VARCHAR(8),
  email VARCHAR(20),
  addr VARCHAR(50),
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS orders;

CREATE TABLE orders (
  id INT NOT NULL AUTO_INCREMENT,
  toy_id INT NOT NULL,
  qty INT,
  user_id INT,
  PRIMARY KEY (id)
);
