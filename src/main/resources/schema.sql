-- DATASOURCE: jdbc:postgresql://ec2-34-194-215-27.compute-1.amazonaws.com:5432/d1c41pd01apg61

-- Username: qnkxqzewessfzz

-- Password: bcc9047c9a6b94fdbe8eabd8b034422a720a47be35bcf9412ea8b009ad8eeaef

create table user_table(
	id	 	 SERIAL 	  not null Primary Key,
	email    varchar(100) not null UNIQUE,
	f_name 	 varchar(100) not null,
	l_name 	 varchar(100) not null,
	password varchar(100) not null,
	phone	 varchar(20)  not null,
	birthday varchar(20)  not null,
	enabled  boolean      not null
);

create table authorities (
	userEmail varchar(100) not null REFERENCES user_table(email) ON UPDATE CASCADE,
	authority varchar(100) not null
);
create unique index ix_auth_username on authorities (userEmail,authority);


create table product (
	id          INT NOT NULL Primary Key,
  	title       VARCHAR(100) NOT NULL,
  	brand       VARCHAR(100) NOT NULL,
  	img	    VARCHAR(200) DEFAULT('https://islandpress.org/sites/default/files/default_book_cover_2015.jpg'),
  	description VARCHAR(500) NOT NULL,
  	weight	    VARCHAR(100) NOT NULL,
	price    FLOAT NOT NULL,
	catId	    INT DEFAULT (1)

);

create table category (
	title	VARCHAR(100) NOT NULL,
	id	SERIAL NOT NULL PRIMARY KEY
);

create table address (
	userEmail	VARCHAR(100) NOT NULL REFERENCES user_table(email) ON UPDATE CASCADE,
	title		VARCHAR(100),
	streetAddress VARCHAR(100),
	City		VARCHAR(100),
	unitNumber	INT,
	PRIMARY KEY (streetAddress,userEmail)
);
create table cartItem (
	cartId		INT NOT NULL,
	productId	INT NOT NULL,
	qty		    INT NOT NULL DEFAULT 1,
	price		INT NOT NULL,
	PRIMARY KEY (cartId, productId)
);
create table myCart(
	id 		    SERIAL NOT NULL Primary Key,
	userId 		varchar(100) REFERENCES user_table(id),
	status		int NOT NULL DEFAULT 0,	-- New, Cart, Checkout, Paid, Complete, and Abandoned.
	sessionId	VARCHAR(100) NOT NULL
);

create table review (
	id          SERIAL NOT NULL Primary Key,
 	productId   INT NOT NULL,	
 	text        VARCHAR(200) NOT NULL,
  	stars	    FLOAT NOT NULL,
  	userId 	    VARCHAR(100) REFERENCES user_table(id)
);

create table orders (
	id		    SERIAL NOT NULL Primary Key,
	userEmail	VARCHAR(100) NOT NULL REFERENCES user_table(email) ON UPDATE CASCADE,
	status 		int NOT NULL,	-- New, Checkout, Paid, Failed, Shipped, Delivered, Returned, and Complete.
	total		FLOAT NOT NULL DEFAULT 0,
	streetAddress VARCHAR(100) NOT NULL,
	timePlaced  VARCHAR(100) NOT NULL,
	sessionId	VARCHAR(100) NOT NULL
);
create table orderItem (
	orderId		INT NOT NULL,
	productId	INT NOT NULL,
	qty		    INT NOT NULL DEFAULT 1,
	price		INT NOT NULL,
	PRIMARY KEY (orderId, productId)
);

create table transaction (
	id			SERIAL NOT NULL Primary Key,
	userEmail	VARCHAR(100) NOT NULL REFERENCES user_table(email) ON UPDATE CASCADE,
	orderId		INT NOT NULL,
	type		INT NOT NULL DEFAULT 0,	-- CREDIT OR DEBIT
	status		INT NOT NULL DEFAULT 0 -- New, Cancelled, Failed, Pending, Declined, Rejected, and Success
);
alter table review
  add constraint book_review_fk foreign key (productId)
  references product (id);

alter table transaction
  add constraint transaction_order_fk foreign key (orderId)
  references orders (id);
 
 alter table cartItem
  add constraint cart_product_fk foreign key (productId)
  references product (id);

alter table cartItem
  add constraint cart_fk foreign key (cartId)
  references myCart (id);

 alter table orderItem
    add constraint order_product_fk foreign key (productId)
  references product (id);

 alter table orderItem
  add constraint orderItem_order_fk foreign key (orderId)
  references orders (id);
  
insert into category(title)VALUES('Misc.');
insert into category(title)VALUES('Alcohol');
insert into category(title)VALUES('Fruits & Veg');
insert into category(title)VALUES('Household');
insert into category(title)VALUES('Beverages');
insert into category(title)VALUES('Granola & Cereals');
insert into category(title)VALUES('Canned Goods');
insert into category(title)VALUES('Cookies & snacks');
  
  
insert into user_table (email,f_name,l_name,password,phone,birthday,enabled) VALUES ('jonnycaliba@gmail.com','Nick','De Luca','$2a$10$/yyvhGpm.GSIrlFO20toHOS08L0dPtPOgw5UXGG7c2fTY8vY/72.e','9054070953','21/03/00',1);
insert into authorities (userEmail,authority) VALUES ('jonnycaliba@gmail.com','ROLE_USER');
insert into authorities (userEmail,authority) VALUES ('jonnycaliba@gmail.com','ROLE_MANAGER');
  
  
  
insert into Product (id,title,brand,img,description,weight,minPrice,maxPrice,catId) VALUES (2343245,'captain crunch','Quaker Oats Company','https://az836796.vo.msecnd.net/media/image/product/en/medium/0005557710666.jpg','best cereal ever created','350g',3.00,7.99,6);
insert into Product (id,title,brand,img,description,weight,minPrice,maxPrice,catId) VALUES (233445,'captain crunch','Quaker Oats Company','https://az836796.vo.msecnd.net/media/image/product/en/medium/0005557710666.jpg','best cereal ever created','350g',3.00,7.99,6);
insert into Product (id,title,brand,img,description,weight,minPrice,maxPrice,catId) VALUES (23534545,'captain crunch','Quaker Oats Company','https://az836796.vo.msecnd.net/media/image/product/en/medium/0005557710666.jpg','best cereal ever created','350g',3.00,7.99,6);
insert into Product (id,title,brand,img,description,weight,minPrice,maxPrice,catId) VALUES (23567245,'captain crunch','Quaker Oats Company','https://az836796.vo.msecnd.net/media/image/product/en/medium/0005557710666.jpg','best cereal ever created','350g',3.00,7.99,6);
insert into Product (id,title,brand,img,description,weight,minPrice,maxPrice,catId) VALUES (236755,'captain crunch','Quaker Oats Company','https://az836796.vo.msecnd.net/media/image/product/en/medium/0005557710666.jpg','best cereal ever created','350g',3.00,7.99,6);
 
  
  
  