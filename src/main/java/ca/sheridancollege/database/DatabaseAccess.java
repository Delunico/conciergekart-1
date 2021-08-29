package ca.sheridancollege.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ca.sheridancollege.beans.Product;
import ca.sheridancollege.beans.Review;
import ca.sheridancollege.beans.User;
import ca.sheridancollege.beans.Category;
import ca.sheridancollege.beans.MyCart;
import ca.sheridancollege.beans.Orders;

@Repository
public class DatabaseAccess {

	@Autowired
	private NamedParameterJdbcTemplate jdbc;
	/**
	 * This method gets all the approved authorities from
	 * the authority table in the h2 database
	 * @return returns a list of authorities as strings
	 */
	public List<String> getAuthorities(){
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = "SELECT DISTINCT authority FROM authorities";
		
		List<String> authorities = jdbc.queryForList(query, params, String.class);
		
		return authorities;
	}
	public double getSubtotal(MyCart cart){
		
		MapSqlParameterSource params = new MapSqlParameterSource();

		String query = "SELECT SUM(price) FROM cartitem where cartId = :cartId";
		params
		.addValue("cartId", cart.getId());
		
		double subtotal = (jdbc.queryForObject(query, params, Double.class)==null) ? 0.0 : jdbc.queryForObject(query, params, Double.class);
		
		return subtotal;
	}
	/**
	 * This method gets all reviews from specified book from 
	 * the review table in the h2 database
	 * @param bookID the id of the specified book
	 * @return a list of reviews for that book 
	 */
	public List<Review> getReviews(long productId){
		
		MapSqlParameterSource params = new MapSqlParameterSource();

		String query = "SELECT * FROM review WHERE productId = :productId ";
		params
		.addValue("productId", productId);
		
		BeanPropertyRowMapper<Review> reviewMapper = 
				new BeanPropertyRowMapper<Review>(Review.class);
		
		List<Review> reviews = jdbc.query(query, params, reviewMapper);
		
		return reviews;
	}
	/**
	 * 
	 * @param bookID
	 * @return
	 */
	public double aveReviews(long productId){
		MapSqlParameterSource params = new MapSqlParameterSource();
		params
		.addValue("productId", productId);
		String query = "SELECT CAST(AVG(stars) as DECIMAL(10,1)) FROM review WHERE review.productId = :productId";

		List<String> aveStar = jdbc.queryForList(query, params, String.class);
		if(aveStar.get(0) != null)
			return Double.parseDouble(aveStar.get(0));
		else
			return 0.0;
	}
	/**
	 * This method gets all the books from the books table
	 * in the h2 database
	 * @return a list of all books 
	 */
	public List<Product> getProducts(boolean filterEnabled){
		
		String query = filterEnabled? "SELECT * FROM Product where enabled = true" : "SELECT * FROM Product";
		
		BeanPropertyRowMapper<Product> mapper = 
				new BeanPropertyRowMapper<Product>(Product.class);
		
		List<Product> products = jdbc.query(query, mapper);
		for (Product product : products) {
			product.setAveStars(this.aveReviews(product.getId()));
		}
		return products;
	}
	
	public List<String> allProducts() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = "SELECT * FROM Product";
		
		BeanPropertyRowMapper<Product> mapper = 
				new BeanPropertyRowMapper<Product>(Product.class);
		
		List<Product> products = jdbc.query(query, params, mapper);
		ArrayList<String> allProducts = new ArrayList<String>();
		for (Product product : products) {
			allProducts.add(product.getTitle()); 
		}
		return allProducts;
	}
	/**
	 * This method adds a given book to the book table
	 * in the h2 database
	 * @param book the given book object to add
	 */
	public void addProduct(Product product) {
	
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		String query = 
				"INSERT INTO product (id,title, brand, img, description, weight, price, catId, country, alcohol_vol) "
				+ "VALUES (:id, :title, :brand, :img, :description, :weight, :price, :catId, :country, :alcohol_vol)";
		
		namedParameters
			.addValue("id", product.getId())
			.addValue("title", product.getTitle())
			.addValue("brand", product.getBrand())
			.addValue("img", product.getImg())
			.addValue("description", product.getDescription())
			.addValue("weight", product.getWeight())
			.addValue("price", product.getPrice())
			.addValue("catId", product.getCatId())
			.addValue("country", product.getCountry())
			.addValue("alcohol_vol", product.getAlcohol_vol());
			
		jdbc.update(query, namedParameters);
		
	}
	
	/**
	 * This method get a specific book based on a given id
	 * from the book table in the h2 database
	 * @param bookID the id of the book to get
	 * @return the book object that corresponds to the given id
	 */
	public Product getProduct(long productId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = "SELECT * FROM product WHERE id = :productId";
		
		params.addValue("productId", productId);
		
		BeanPropertyRowMapper<Product> mapper = 
				new BeanPropertyRowMapper<Product>(Product.class);
		
		List<Product> products = jdbc.query(query, params, mapper);
		products.get(0).setAveStars(this.aveReviews(products.get(0).getId()));
		return products.get(0);
		
	}
	public User getUser(String email) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = "SELECT * FROM user_table WHERE email = :email";
		params
		.addValue("email", email);
		
		BeanPropertyRowMapper<User> userMapper = 
				new BeanPropertyRowMapper<User>(User.class);
		
		List<User> user = jdbc.query(query, params, userMapper);
		
		if(user.isEmpty()) {
			return null;
		}else {
			return user.get(0);
		}
	}
	public User getUser(long userId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = "SELECT * FROM user_table WHERE id = :userId";
		params
		.addValue("userId", userId);
		
		BeanPropertyRowMapper<User> userMapper = 
				new BeanPropertyRowMapper<User>(User.class);
		
		List<User> user = jdbc.query(query, params, userMapper);
		
		if(user.isEmpty()) {
			return null;
		}else {
			return user.get(0);
		}
	}

	/**
	 * This method adds a review to the review table in 
	 * the h2 database with a given bookID and review
	 * @param bookID the id of book to add a review for
	 * @param review the text of the review to add
	 */
	public void addReview(Review review) {
			
			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			String query = 
					"INSERT INTO review (productId, text, stars, userId) VALUES (:productId, :review, :stars, :userId)";
			namedParameters
				.addValue("productId", review.getProductId())
				.addValue("review", review.getText())
				.addValue("stars", review.getStars())
				.addValue("userId", review.getUserId());
				
			jdbc.update(query, namedParameters);
			
	}
	public void addUser(User user) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		String query = 
				"INSERT INTO user_table (f_name,l_name,password,email,phone,birthday,enabled) "
				+ "VALUES (:f_name,:l_name, :password, :email, :phone, :birthday, :enabled)";
		String query2 =
				"INSERT INTO authorities (userEmail, authority) "
				+ "VALUES (:email, :authority)";
		
		namedParameters
			.addValue("f_name", user.getF_name())
			.addValue("l_name", user.getL_name())
			.addValue("password", user.getPassword())
			.addValue("email", user.getEmail())
			.addValue("phone", user.getPhone())
			.addValue("birthday", user.getBirthYear())
			.addValue("enabled", true)
			.addValue("authority", "ROLE_USER");

		jdbc.update(query, namedParameters);
		jdbc.update(query2, namedParameters);
		
	}
	public List<Product> searchProducts(String search) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("search", search);
		
		String query = "SELECT * FROM product WHERE title ilike '%' || (:search) || '%'";
		
		BeanPropertyRowMapper<Product> mapper = 
				new BeanPropertyRowMapper<Product>(Product.class);
		
		List<Product> products = jdbc.query(query, params, mapper);
		
		return products;
	}
	public boolean recoverPassword(String email,String newPassword) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = "SELECT email FROM user_table WHERE email = :email";
		String query2 = 
				"UPDATE user_table SET password = :password WHERE email = :email ";
		params
		.addValue("email", email)
		.addValue("password", newPassword);
		
		BeanPropertyRowMapper<User> UserMapper = 
				new BeanPropertyRowMapper<User>(User.class);
		
		List<User> users = jdbc.query(query, params, UserMapper);
		
		if(users.isEmpty()) {
			return false;
		}else {
			jdbc.update(query2, params);
			return true;
		}
		
	}
	public void updateItemStatus(long productid,boolean status) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = 
				"UPDATE product SET enabled = :status WHERE id = :pid";
		
		params
		.addValue("pid", productid)
		.addValue("status", status);

		jdbc.update(query, params);
	}
	
	public MyCart getCart(HttpSession session, long userId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = "SELECT * FROM myCart WHERE userId = :userId AND status = 0";
		params
		.addValue("userId", userId);
		
		BeanPropertyRowMapper<MyCart> mapper = 
				new BeanPropertyRowMapper<MyCart>(MyCart.class);
		
		List<MyCart> cart = jdbc.query(query, params, mapper);  
		
		if(!cart.isEmpty()) {
			updateCartSession(session,userId);
			cart = jdbc.query(query, params, mapper); 
			return cart.get(0);
		}else {
			return createCart(userId, 0, session);
		}
	}
	private void updateCartSession(HttpSession session, long userId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = 
				"UPDATE myCart SET sessionId = :sessionId WHERE userId = :userId AND status = 0";
		
		params
		.addValue("userId", userId)
		.addValue("sessionId", session.getId());

		jdbc.update(query, params);
	}
	private void updateCartStatus(MyCart cart, int status) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = 
				"UPDATE myCart SET status = :status WHERE id = :cartId";
		
		params
		.addValue("cartId", cart.getId())
		.addValue("status", status);

		jdbc.update(query, params);
	}
	private MyCart createCart(long userId, int status, HttpSession session) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = 
				"INSERT INTO myCart (userId,status,sessionId) VALUES (:userId, :status, :sessionId)";
		params
			.addValue("userId", userId)
			.addValue("sessionId", session.getId())
			.addValue("status", status);
			
		jdbc.update(query, params);
		
		return getCart(session, userId);
	}
	
	public void addCartItem(MyCart cart, long productId, long userId, long qty, double price, String winebox, String size) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		String query = 
				"INSERT INTO cartItem (cartId,productId,qty,price,winebox, size) VALUES (:cartId, :productId, :qty, :price,:winebox, :size)";
		namedParameters
			.addValue("price", price)
			.addValue("productId", productId)
			.addValue("cartId", cart.getId())
			.addValue("qty", qty)
			.addValue("winebox", winebox)
			.addValue("size", size);
			
		jdbc.update(query, namedParameters);
		
	}
	public List<Product> getMyCartItems(MyCart cart) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = 
				  "SELECT PRODUCT.id,title,brand,img,description,weight,catId,cartItem.qty,cartItem.price, cartItem.winebox, cartItem.size"
				+ " FROM PRODUCT"
				+ " join cartItem"
				+ " on PRODUCT.id = cartItem.productId"
				+ " WHERE cartId = :cartId";
		params
		.addValue("cartId", cart.getId());
		
		BeanPropertyRowMapper<Product> mapper = 
				new BeanPropertyRowMapper<Product>(Product.class);
		
		List<Product> products = jdbc.query(query, params, mapper);
		
		return products;	
	}
	
	public void removeCartItem(long productid, MyCart cart) {
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		
		String query = 
				"DELETE FROM cartItem WHERE productid = :productid AND cartid = :cartId";
				
		namedParameters
			.addValue("productid", productid)
			.addValue("cartId", cart.getId());

		jdbc.update(query, namedParameters);
	}
	
	public void updateUser(User user) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = 
				"UPDATE user_table SET f_name = :f_name, l_name = :l_name, email = :email, phone = :phone ";
		String where ="WHERE id = :userId";
		params
		.addValue("userId", user.getId())
		.addValue("f_name", user.getF_name())
		.addValue("l_name", user.getL_name())
		.addValue("phone", user.getPhone())
		.addValue("email", user.getEmail())
		.addValue("password", user.getPassword());
		
		if(user.getPassword() !=null) 
			query += ", password = :password ";
				
		query += where;
		
		jdbc.update(query, params);
		
	}

	public List<Category> getCategories() {
		
		String query = "SELECT * FROM CATEGORY";
		
		BeanPropertyRowMapper<Category> mapper = 
				new BeanPropertyRowMapper<Category>(Category.class);
		
		List<Category> categories = jdbc.query(query, mapper);
		return categories;	
	}
	public Orders getActiveOrder(User user) {
		for (Orders order : this.getMyOrders(user)) {
			if(order.getStatus() ==0) {
				return order;
			}
		}
		return null;
	}
	public List<Orders> getActiveOrders() {
	
		String query = "SELECT * FROM ORDERS where status = 0 ORDER BY driverId, timePlaced DESC";
	
		BeanPropertyRowMapper<Orders> mapper = 
				new BeanPropertyRowMapper<Orders>(Orders.class);
		
		List<Orders> orders = jdbc.query(query,mapper);
		return orders;	
	}
	public List<Orders> getActiveOrders(long userId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params
		.addValue("userId", userId);
		String query = "SELECT * FROM ORDERS where driverId = :userId ORDER BY status, timePlaced DESC LIMIT 15";
	
		BeanPropertyRowMapper<Orders> mapper = 
				new BeanPropertyRowMapper<Orders>(Orders.class);
		
		List<Orders> orders = jdbc.query(query,params,mapper);
		return orders;	
	}
	public Orders getOrder(MyCart cart, User user) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = "SELECT * FROM ORDERS where userEmail = :userEmail AND cartId = :cartId";
		params
		.addValue("userEmail", user.getEmail())
		.addValue("cartId", cart.getId());
		BeanPropertyRowMapper<Orders> mapper = 
				new BeanPropertyRowMapper<Orders>(Orders.class);
		
		List<Orders> orders = jdbc.query(query, params,mapper);
		return orders.get(0);	
	}
	public Orders getOrder(String orderId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = "SELECT * FROM ORDERS where id = :orderId";
		params
		.addValue("orderId", Long.parseLong(orderId));
		BeanPropertyRowMapper<Orders> mapper = 
				new BeanPropertyRowMapper<Orders>(Orders.class);
		
		List<Orders> orders = jdbc.query(query, params,mapper);
		return orders.get(0);	
	}
	public void createOrder(MyCart cart, User user, String address) {
		this.updateCartStatus(cart,1);
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		TimeZone zone = TimeZone.getTimeZone("US/Eastern");
		formatter.setTimeZone(zone);
		Date d = new Date();
		MapSqlParameterSource namedParameters = new MapSqlParameterSource();
		String query = 
			"INSERT INTO orders (userEmail, status, total, streetAddress, timePlaced, sessionId, cartId) VALUES (:email, :status, :total, :streetAddress, :timePlaced, :sessionId, :cartId)";
			namedParameters
				.addValue("email", user.getEmail())
				.addValue("status", cart.getStatus())
				.addValue("total",this.getSubtotal(cart))
				.addValue("streetAddress",address)
				.addValue("timePlaced",formatter.format(d))
				.addValue("sessionId",cart.getSessionId())
				.addValue("cartId",cart.getId());
			jdbc.update(query, namedParameters);
			
		List<Product> products = this.getMyCartItems(cart);
		for (Product p : products) {
			MapSqlParameterSource cartitemParams = new MapSqlParameterSource();
			String query2 = 
					"INSERT INTO orderItem (orderId,productId,qty,price, winebox, size) VALUES (:orderId, :productId, :qty, :price, :winebox, :size)";
			cartitemParams
			.addValue("winebox", p.getWinebox())
			.addValue("size", p.getSize())
			.addValue("price", p.getPrice())
			.addValue("productId", p.getId())
			.addValue("cartId", cart.getId())
			.addValue("qty", p.getQty())
			.addValue("orderId", this.getOrder(cart, user).getId());
				
			jdbc.update(query2, cartitemParams);
		}
		String query3 = 
				"DELETE FROM cartItem WHERE cartId  = :cartId";
		jdbc.update(query3, namedParameters);

	}
	
	public String totalEarnings(User user){
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = "SELECT SUM(total) FROM orders WHERE driverId = :userId";
		params
		.addValue("userId", user.getId());
		String total = jdbc.queryForObject(query, params, String.class);
		return total;
	}
	
	public List<Orders> getMyOrders(User user) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		String query = "SELECT * FROM orders WHERE userEmail = :userEmail ORDER BY status, timePlaced DESC LIMIT 15";
		params
		.addValue("userEmail", user.getEmail());
		
		BeanPropertyRowMapper<Orders> mapper = 
				new BeanPropertyRowMapper<Orders>(Orders.class);
		
		List<Orders> orders = jdbc.query(query, params, mapper);  
		
		return orders;
	}
	//available orders
	public List<Product> getOrderItems(List<Orders> activeOrders) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		BeanPropertyRowMapper<Product> mapper = 
				new BeanPropertyRowMapper<Product>(Product.class);
		String query = 
				  "SELECT PRODUCT.id,title,brand,img,description,weight,catId,orderItem.qty,orderItem.price,orderItem.orderId, orderItem.winebox, orderItem.size"
				+ " FROM PRODUCT"
				+ " join orderItem"
				+ " on PRODUCT.id = orderItem.productId"
				+ " WHERE orderId = :orderId";
		
		List<Product> orderItems = new ArrayList<Product>();;
		for (Orders order : activeOrders) {
			params
			.addValue("orderId", order.getId());
			 orderItems.addAll(jdbc.query(query, params, mapper));
		}
		
		return orderItems;
	}
	//my orders
	public List<Product> getOrderItems(Orders order) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		BeanPropertyRowMapper<Product> mapper = 
				new BeanPropertyRowMapper<Product>(Product.class);
		String query = 
				  "SELECT PRODUCT.id,title,brand,img,description,weight,catId,orderItem.qty,orderItem.price,orderItem.orderId, orderItem.winebox, orderItem.size"
				+ " FROM PRODUCT"
				+ " join orderItem"
				+ " on PRODUCT.id = orderItem.productId"
				+ " WHERE orderId = :orderId";
		
		List<Product> orderItems = new ArrayList<Product>();;
		params
			.addValue("orderId", order.getId());
			orderItems.addAll(jdbc.query(query, params, mapper));
		
		return orderItems;
	}
	public void selectOrder(long userId, long orderId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = 
				"UPDATE orders SET driverId = :userId Where id = :orderId";
		params
		.addValue("orderId", orderId)
		.addValue("userId", userId);
		
		jdbc.update(query, params);
	}
	public void completeOrder(long userId, long orderId) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		String query = 
				"UPDATE orders SET status = 1 Where id = :orderId";
		params
		.addValue("orderId", orderId)
		.addValue("userId", userId);
		
		jdbc.update(query, params);
	}
}
