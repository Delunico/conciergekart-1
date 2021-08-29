package ca.sheridancollege.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ca.sheridancollege.beans.MyCart;
import ca.sheridancollege.beans.Orders;
import ca.sheridancollege.beans.Product;
import ca.sheridancollege.beans.Review;
import ca.sheridancollege.beans.User;
import ca.sheridancollege.database.DatabaseAccess;
import ca.sheridancollege.mail.MailClient;

@Controller
public class HomeController {

	@Autowired
	DatabaseAccess da;

	@Autowired
	private MailClient mailClient;

	@Autowired
	@Lazy
	private BCryptPasswordEncoder passwordEncoder;
	
	@PostMapping("recover")
	public String recoverAccount(Model model,@RequestParam String email) {
		String newPassword = generatePassword();
		String encodedPassword = passwordEncoder.encode(newPassword);
		String message = "Your new password is "+ newPassword;
		if(da.recoverPassword(email,encodedPassword)) {
			sendEmail("elixirhooch@gmail.com",email,"ConciergeCart Password Recovery", message);
			
			model.addAttribute("message","A password recovery email has been successfully sent and should arrive momentarily");
		}else {
			model.addAttribute("message","Email does not exist");
			return "recover-account";
		}
		model.addAttribute("allproducts",da.allProducts());
		return "login";
	}
	@GetMapping("user/checkout")
	public String checkout(Model model,HttpSession session, Authentication auth) {
		if(auth != null) {
			User user = da.getUser(auth.getName());
			MyCart cart = da.getCart(session, user.getId());
			List<Product> products = da.getMyCartItems(cart);
			model.addAttribute("username",user.getF_name());
			model.addAttribute("products",products);
			model.addAttribute("total",(int)da.getSubtotal(cart));
			model.addAttribute("cart_qty",products.size());
			model.addAttribute("allproducts",da.allProducts());
		}
		return "user/order";
	}
	@GetMapping("placeOrder")
	public String placeOrderGet() {
		return "redirect:/permission-denied";
	}
	
	@PostMapping("placeOrder")
	public String placeOrder(Model model,HttpSession session, Authentication auth, 
			@RequestParam String streetAddress, @RequestParam String city,
			@RequestParam String unitNumber) {
		String address = (unitNumber!="") ? streetAddress +" "+city+" Unit: "+unitNumber : streetAddress +" "+city;
		if(auth != null) {
			User user = da.getUser(auth.getName());
			MyCart cart = da.getCart(session, user.getId());
			List<Orders> orders = da.getMyOrders(user);
			model.addAttribute("username",user.getF_name());
			model.addAttribute("allproducts",da.allProducts());
			model.addAttribute("categories",da.getCategories());
			
			if(orders !=null) {
				for (Orders order : orders) {
					if(order.getStatus()==0) {
						List<Product> products = da.getMyCartItems(cart);
						model.addAttribute("cart_qty",products.size());
						model.addAttribute("message","Sorry! You can't have more than one pending order at a time: Please contact your driver for order modifications");
						model.addAttribute("orders",orders);
						return "user/orders";
					}
				}
			}
			model.addAttribute("message","Your order has been placed and a confirmation email will be sent shortly Thank You!");
			da.createOrder(cart, user, address);
			orderConfirmation("elixirhooch@gmail.com",user.getEmail(), "Order Confirmation", 
					"Your order was successfully placed, we will update you once a driver is on their way", 
					da.getActiveOrder(user), da.getOrderItems(da.getActiveOrder(user)));
			List<User> drivers = new ArrayList<User>();
			drivers.add(da.getUser(1));
			drivers.add(da.getUser(37));
			drivers.add(da.getUser(38));
			String message = "A new order was placed by " + user.getF_name();
			for (User d : drivers) {
				orderConfirmation("elixirhooch@gmail.com",d.getEmail(),"New Order from " + user.getF_name(),message, 
						da.getActiveOrder(user), da.getOrderItems(da.getActiveOrder(user)) );
			}
			
			model.addAttribute("orders",da.getMyOrders(user));
			
			
			return "user/orders";
		}
		return "redirect:/permission-denied";
	}
	public void setPageData(Authentication auth, Model model, HttpSession session) {
		User user = da.getUser(auth.getName());
		MyCart cart = da.getCart(session, user.getId());
		List<Product> products = da.getMyCartItems(cart);
		model.addAttribute("cart_qty",products.size());
		model.addAttribute("username",user.getF_name());
		model.addAttribute("products",products);
		model.addAttribute("allproducts",da.allProducts());
	}
	@GetMapping("user/myOrders")
	public String goMyOrders(Authentication auth, Model model, HttpSession session) {
		User user = da.getUser(auth.getName());
		MyCart cart = da.getCart(session, user.getId());
		List<Product> products = da.getMyCartItems(cart);
		
		model.addAttribute("cart_qty",products.size());
		model.addAttribute("username",user.getF_name());
		model.addAttribute("orders",da.getMyOrders(user));
		model.addAttribute("allproducts",da.allProducts());
		Orders activeOrder = da.getActiveOrder(user);
		if(activeOrder != null) {
			if(activeOrder.getDriverId() != 0) {
				model.addAttribute("driver_contact",da.getUser(activeOrder.getDriverId()).getPhone());
			}else {
				model.addAttribute("message","we'll notify you once a driver has accepted your order");
			}
		}
		return "user/orders";
	}
	@PostMapping("selectOrder")
	public String selectOrder(Authentication auth, @RequestParam String id) {
		if(auth!=null) {
			User user = da.getUser(auth.getName());
			if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
				da.selectOrder(user.getId(),Long.parseLong(id));
				String message = user.getF_name() + " is delivering your order, call or text " + user.getPhone() + 
						" for any order changes or special requests";
				sendEmail("elixirhooch@gmail.com", da.getOrder(id).getUserEmail(),"A Driver Is On Their Way",message);
			}
		}
		return "redirect:/admin/myActiveOrders";
	}
	@PostMapping("completeOrder")
	public String completeOrder(Authentication auth, @RequestParam String id) {
		User user = da.getUser(auth.getName());
		if(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
			da.completeOrder(user.getId(),Long.parseLong(id));
		}
		return "redirect:/admin/myActiveOrders";
	}
	@GetMapping("admin/myActiveOrders")
	public String goMyOrdersActive(Authentication auth, Model model, HttpSession session) {
		User user = da.getUser(auth.getName());
		MyCart cart = da.getCart(session, user.getId());
		List<Product> products = da.getMyCartItems(cart);
		List<Orders> orders = da.getActiveOrders(user.getId());
		List<User> customers = new ArrayList<User>();
		for (Orders order : orders) {
			customers.add(da.getUser(order.getUserEmail()));
		}
		List<User> distinct = customers.stream().distinct().collect(Collectors.toList());
		model.addAttribute("customers",distinct);
		model.addAttribute("active",true);
		model.addAttribute("orderItems",da.getOrderItems(da.getActiveOrders(user.getId())));
		model.addAttribute("orders",orders);
		model.addAttribute("username",user.getF_name());
		model.addAttribute("cart_qty",products.size());
		model.addAttribute("allproducts",da.allProducts());
		return"admin/orders";
	}
	@GetMapping("admin/myOrders")
	public String goMyOrdersAdmin(Authentication auth, Model model, HttpSession session) {
		if(auth != null) {
			User user = da.getUser(auth.getName());
			List<Orders> orders = da.getActiveOrders();
			User[] drivers = {da.getUser(38),da.getUser(37),da.getUser(1)};
			List<User> customers = new ArrayList<User>();
			for (Orders order : orders) {
				customers.add(da.getUser(order.getUserEmail()));
			}
			List<User> distinct = customers.stream().distinct().collect(Collectors.toList());
			model.addAttribute("customers",distinct);
			model.addAttribute("drivers",drivers);
			model.addAttribute("orderItems",da.getOrderItems(da.getActiveOrders()));
			model.addAttribute("orders",orders);
			model.addAttribute("username",user.getF_name());
			model.addAttribute("allproducts",da.allProducts());
		}
		return "admin/orders";
	}
	@GetMapping("user/myCart")
	public String goMyCart(Authentication auth, Model model, HttpSession session) {
		
		if(auth != null) {
			User user = da.getUser(auth.getName());
			MyCart cart = da.getCart(session, user.getId());
			List<Product> products = da.getMyCartItems(cart);
			
			model.addAttribute("products",products);
			model.addAttribute("username",user.getF_name());
			model.addAttribute("cart_qty",products.size());
			if(products.size()>0) {
				model.addAttribute("total", da.getSubtotal(cart));
			}	
		}
		model.addAttribute("allproducts",da.allProducts());
		return "user/myCart";
	}
	//SPIRITS
	public double calcPrice(int quantity, int size,Product product){
		double amount = 0;
		switch (size) {
			case(375):
				amount = product.getPrice();; 
			break;
			case(750):
				amount = product.getPrice() + 25; 
			break;
			case(1140):
				amount = product.getPrice() + 40; 
			break;
		}
		if(product.getWeight().length() <= 6){
			amount = product.getPrice();
		}
		return amount * quantity;
	}
	//Wine and Beer
	public double calcPrice(int quantity, Product product){
		double amount = product.getPrice();
		if(quantity<=3) {//wine, single weight option spirit
			amount = product.getPrice() * quantity;
		}
		else if(quantity<=24){ 
			amount = product.getPrice() + (15.00*((quantity/6)-1));
		}
		else if(quantity==36){
			amount = 125;
		}
		else if(quantity==48){
			amount = 155;
		}
		else if(quantity==60){
			amount = 195;
		}
		else if(quantity==72){
			amount = 230;
		}
		if(product.getTitle().contains("Tall Boys") || product.getWeight().contains("473")){//pilsner // white claw //drop jaw
			if(quantity >= 48 ) amount += 10;
			if(quantity==72) return 245;
		}
		return amount;
	}
	@PostMapping("addCartItem/{productId}")
	public String addCartItem(Model model, @PathVariable long productId,
			Authentication auth,HttpSession session, @RequestParam int qty, @RequestParam String winebox, 
			@RequestParam String size) {
			
		if(auth != null) {
			User user = da.getUser(auth.getName());
			MyCart cart = da.getCart(session, user.getId());
			Product p = da.getProduct(productId);
			double price = p.getPrice();
			if(p.getCatId()==21) {
				price = calcPrice(qty,Integer.parseInt(size),p);
			}else {
				price = calcPrice(qty,p);
			}
			
			da.addCartItem(cart, productId,user.getId(),qty, price, winebox, size);
			model.addAttribute("username",user.getF_name());
		}
		return "redirect:/user/myCart";
	}
	@GetMapping("removeCartItem/{productId}")
	public String deleteMyCartItem(Model model, @PathVariable long productId,
			Authentication auth,HttpSession session,HttpServletRequest request) {
		if(auth != null) {
			String userName = auth.getName();
			MyCart cart = da.getCart(session, da.getUser(userName).getId());
			da.removeCartItem(productId,cart);
			model.addAttribute("username",userName);
			String referer = request.getHeader("Referer");
			return "redirect:"+ referer;
		}
		return "redirect:/";
	}
	public String generatePassword() {
		String chars ="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder string = new StringBuilder();
		Random rnd = new Random();
		while(string.length() < 10){
			int index = (int)(rnd.nextFloat() * chars.length());
			string.append(chars.charAt(index));
		}
		String password = string.toString();
		return password;
	}
	public void sendEmail(String from,String recipient, String subject, String text) {
		
		mailClient.prepareAndSend(from, recipient, subject, text);
	}
	public void orderConfirmation(String from,String recipient, String subject, String text, Orders order, List<Product> cartItems) {
		
		mailClient.orderConfirmation(from, recipient, subject, text, order, cartItems);
	}
	
	/**
	 * This method adds new users to the database
	 * if the username does not already exist
	 * @param userName the new username to add arriving as a post request
	 * @param password the new username to add arriving as a post request
	 * @param authorities the selected authority approved for registering a new user
	 * @param model where to store a message, authorities, and books
	 * @return the name of the template for the home page
	 */
	@PostMapping("addUser")
	public String addUser(
			@RequestParam String f_name,   @RequestParam String l_name, 
			@RequestParam String password, @RequestParam String email, 
			@RequestParam String phone,    @RequestParam int birthYear, Model model) {
		
		model.addAttribute("allproducts",da.allProducts());
		try {
			Date d = new Date();
			int year = d.getYear() + 1900;
			if ((year - 19) < birthYear) {
				throw new InputMismatchException();
			}
			List<GrantedAuthority> authorityList = new ArrayList<>();
			authorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
			
			String encodedPassword = passwordEncoder.encode(password);
			User user = new User(f_name,l_name, encodedPassword, email, phone, birthYear, authorityList);
			da.addUser(user);
			model.addAttribute("message", "User successfully added");

		} catch (InputMismatchException i) {
			List<String> allAuthorities = da.getAuthorities();
			model.addAttribute("message","You must be at least 19 years of age to use these services");
			model.addAttribute("authorities", allAuthorities);
			return "register";
		} catch (Exception e) {
		
			System.out.println(e.getMessage());
			List<String> allAuthorities = da.getAuthorities();
			model.addAttribute("message", "User already exists");
			model.addAttribute("authorities", allAuthorities);
			return "register";
		}
		List<Product> products = da.getProducts(true);
		model.addAttribute("products", products);
		return "login";
	}

	@PostMapping("admin/addProduct")
	public String addProduct(@ModelAttribute Product product) {
		da.addProduct(product);
		return "redirect:/";
	}
	
	@PostMapping("admin/updateProduct")
	public String updateProduct(@ModelAttribute Product product, Model model) {
		da.updateProduct(product);
		return "redirect:/admin/updatePage/"+product.getId();
	}
	
	/**
	 * This method maps to the home template page from the "/" root
	 * @param auth the authorities of the current user
	 * @param model where to store the userName, roles, and books
	 * @return the name of the home template
	 */
	
	@GetMapping("/")
	public String goHome(Authentication auth,Model model,HttpSession session, HttpServletRequest request ) {
		model.addAttribute("over19",false);
		List<String> roles = new ArrayList<>();
		if (auth != null) {
			for (GrantedAuthority authority : auth.getAuthorities()) {
				roles.add(authority.getAuthority());
			}
			User user = da.getUser(auth.getName());
			model.addAttribute("username",user.getF_name());
			model.addAttribute("roles", roles);
			model.addAttribute("cart_qty",da.getMyCartItems(da.getCart(session, user.getId())).size());
		}
		
		List<Product> products = roles.contains("ROLE_MANAGER")? da.getProducts(false) : da.getProducts(true);
		model.addAttribute("categories",da.getCategories());
		model.addAttribute("products", products);
		model.addAttribute("allproducts",da.allProducts());
		return "index";

	}
	/**
	 * This method adds a review to the database and 
	 * returns to the review page after
	 * @param id the id of the product to add a review for
	 * @param review the text of the review to add
	 * @return a redirection to the previous review page
	 */
	@PostMapping("submitReview")
	public String submitReview(@ModelAttribute Review review, @RequestParam long userId, @RequestParam Long productId) {
		review.setUserId(userId);
		review.setProductId(productId);
		da.addReview(review);
		return "redirect:/viewProduct/" + review.getProductId();
	}
	@GetMapping("search")
	public String search(@RequestParam String search, Model model, Authentication auth, HttpSession session) {
		List<Product> products = da.searchProducts(search);
		model.addAttribute("search",search);
		model.addAttribute("products",products);
		if (auth != null) {
			User user = da.getUser(auth.getName());
			model.addAttribute("username", user.getF_name());
			model.addAttribute("cart_qty",da.getMyCartItems(da.getCart(session, user.getId())).size());
		}
		String message = "Search Results for: " + search;
		model.addAttribute("message",message);
		model.addAttribute("allproducts",da.allProducts());
		return "redirect:/viewProduct/" + products.get(0).getId();
	}
	
	@GetMapping("user/addReview/{productId}")
	public String AddReview(@PathVariable long productId, Model model, Authentication auth) {
	
		model.addAttribute("product", da.getProduct(productId));
		model.addAttribute("review", new Review());
		model.addAttribute("userId", da.getUser(auth.getName()).getId());
		model.addAttribute("allproducts",da.allProducts());
		return "user/add-review";
	}
	/**
	 * This method maps to the view-product template and
	 * adds the books reviews and the book itself to the model
	 * @param model where to store the userName, book, and reviews
	 * @param bookID the id of the book to add a review for
	 * @param auth a list of all authorities for the current user 
	 * @return the name of the view reviews page template
	 */
	@GetMapping("viewProduct/{productId}")
	public String viewProduct(Model model, @PathVariable long productId, 
			Authentication auth, HttpSession session) {
	
		List<Review> reviews = da.getReviews(productId);
		model.addAttribute("reviews", reviews);
		model.addAttribute("reviews_size",reviews.size());
		Product product = da.getProduct(productId);
		
		if (auth != null) {
			User user = da.getUser(auth.getName());
			model.addAttribute("username", user.getF_name());
			MyCart cart = da.getCart(session, user.getId());
			List<Product> products = da.getMyCartItems(cart);
			for (Product p : products) {
				if(p.getId()==product.getId()) {
					product.setQty(p.getQty());
					product.setPrice(p.getPrice());
					model.addAttribute("favourited",true);
				}
			}
			model.addAttribute("cart_qty",products.size());
		}
		if(product.getTitle().toLowerCase().contains("box")) {
			String[] wines = 
				{"Pinot Grigio", "sauvignon blanc", "chardonnay",
				"Shiraz", "Cabernet Sauvignon", "Merlot"};
			model.addAttribute("winebox",wines);
		}
		if(product.getCatId()==21) {
			String s = product.getWeight().replace("ml","");
			int [] sizes = Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
			model.addAttribute("sizes", sizes);
		}
		model.addAttribute("product", product);
		int[] beerValues = {6,12,18,24,36,48,60,72};
		int[] values = IntStream.rangeClosed(1, 3).toArray();
		model.addAttribute("beerValues",beerValues);
		model.addAttribute("values",values);
		model.addAttribute("quantity",0);
		model.addAttribute("allproducts",da.allProducts());
		return "view-product";
	}
	
	@GetMapping("admin/disableProduct/{productId}")
	public String disableProduct(Model model, @PathVariable long productId) {
		da.updateItemStatus(productId,false);
		return "redirect:/";
	}
	
	@GetMapping("admin/enableProduct/{productId}")
	public String enableProduct(Model model, @PathVariable long productId) {
		da.updateItemStatus(productId,true);
		return "redirect:/";
	}
	
	@GetMapping("user/account")
	public String goAccount(Authentication auth,Model model,HttpSession session) {

		if(auth !=null) {
			User user = da.getUser(auth.getName());
			model.addAttribute("user",user);
			model.addAttribute("username",user.getF_name());
			model.addAttribute("cart_qty",da.getMyCartItems(da.getCart(session, user.getId())).size());
			model.addAttribute("total",da.totalEarnings(user));
		}
		model.addAttribute("allproducts",da.allProducts());
		return "user/account";
	}
	
	@PostMapping("updateUser/{id}")
	public String updateUser(@PathVariable long id, Model model,@RequestParam String f_name,
			@RequestParam String l_name, @RequestParam String email, 
			@RequestParam String phone, @RequestParam String password, Authentication auth) {
		User user = new User();
		if(password !="")
			user.setPassword(passwordEncoder.encode(password));
		user.setEmail(email);
		user.setF_name(f_name);
		user.setL_name(l_name);
		user.setPhone(phone);
		user.setId(id);
		model.addAttribute("allproducts",da.allProducts());
		try {
			da.updateUser(user);
			
		}catch(Exception e) {
			if(auth!=null)
				System.out.println(e.getMessage());
				model.addAttribute("user",da.getUser(auth.getName()));
				model.addAttribute("username",da.getUser(auth.getName()).getF_name());
			model.addAttribute("message","Email already exists! Try again.");
			return "user/account";
		}
		model.addAttribute("user",da.getUser(email));
		model.addAttribute("username",user.getF_name());
		model.addAttribute("message","User details were sucessfully updated");
		
		auth = SecurityContextHolder.getContext().getAuthentication();
		Authentication newAuth = new UsernamePasswordAuthenticationToken(email, auth.getCredentials(),auth.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuth);
		return "user/account";
	}
	
	@GetMapping("admin/addPage")
	public String addPage(Model model,Authentication auth) {

		model.addAttribute("product", new Product());
		User user = da.getUser(auth.getName());
		model.addAttribute("username",user.getF_name());
		model.addAttribute("allproducts",da.allProducts());
		model.addAttribute("categories",da.getCategories());
		return "admin/add-product";
	}
	@GetMapping("admin/updatePage/{productId}")
	public String updatePage(Model model,Authentication auth, @PathVariable long productId) {

		model.addAttribute("product", da.getProduct(productId));
		User user = da.getUser(auth.getName());
		model.addAttribute("username",user.getF_name());
		model.addAttribute("allproducts",da.allProducts());
		model.addAttribute("categories",da.getCategories());
		return "admin/update-product";
	}
	/**
	 * This method maps the /login get request to the
	 * login template
	 * @return the name of the login template
	 */
	@GetMapping("login")
	public String login(Model model) {	
		model.addAttribute("allproducts",da.allProducts());
		return "login";
	}
	
	/**
	 * This method maps the register request to the register template
	 * and adds the approved user authority to the model for new users
	 * @param model where to store the userAuthority
	 * @return the name of the register template
	 */
	@GetMapping("register")
	public String register(Model model) {
		model.addAttribute("allproducts",da.allProducts());
		return "register";
	}
	/**
	 * This methods maps the permission-denied request to
	 * the /error/permission-denied template
	 * @return the path to the permission-denied template
	 */
	@GetMapping("permission-denied")
	public String error(Model model) {
		
		model.addAttribute("allproducts",da.allProducts());
		return "error/permission-denied";
	}
	@GetMapping("recover-account")
	public String recover(Model model, Authentication auth,HttpSession session) {
		if(auth !=null) {
			User user = da.getUser(auth.getName());
			model.addAttribute("username",user.getF_name());
			model.addAttribute("cart_qty",da.getMyCartItems(da.getCart(session, user.getId())).size());
		}
		model.addAttribute("allproducts",da.allProducts());
		return "recover-account";
	}
	@GetMapping("/privacy")
	public String privacy(Model model, Authentication auth,HttpSession session) {
		if(auth !=null) {
			User user = da.getUser(auth.getName());
			model.addAttribute("username",user.getF_name());
			model.addAttribute("cart_qty",da.getMyCartItems(da.getCart(session, user.getId())).size());
		}
		model.addAttribute("allproducts",da.allProducts());
		return "privacy-policy";
	}
	@GetMapping("no")
	public String notOver19(Model model) {
		model.addAttribute("over19","no");
		return "index";
	}
	
	@PostMapping("contactus")
	public String contactUs(Model model, @RequestParam String email, 
			@RequestParam String message, @RequestParam String name) {
		String subject = name +" sent you a message";
		message += "\n"+"\n name: " + name +  "\n email: " + email;
		sendEmail("elixirhooch@gmail.com","elixirhooch@gmail.com", subject, message);
		return "redirect:/contact-us/success";
	}
	@GetMapping({"contact-us","/contact-us/success"})
	public String ContactUs(Model model, Authentication auth, HttpSession session, HttpServletRequest request) {
		if(auth !=null) {
			User user = da.getUser(auth.getName());
			model.addAttribute("username",user.getF_name());
			model.addAttribute("cart_qty",da.getMyCartItems(da.getCart(session, user.getId())).size());
		}
		model.addAttribute("allproducts",da.allProducts());
		if(request.getRequestURL().toString().contains("success")) {
			model.addAttribute("message","An email has been sent and we'll reply shortly");
		}
		return "contactus";
	}
}
