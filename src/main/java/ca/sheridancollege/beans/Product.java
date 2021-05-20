package ca.sheridancollege.beans;


import org.springframework.beans.factory.annotation.Autowired;

import ca.sheridancollege.database.DatabaseAccess;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Product {
	@Autowired
	DatabaseAccess da;
	
	private long id; 
	private String title;
	private String brand;
	private double aveStars = 0.0;
	private String img;
	private String description;
	private String weight;
	private long price;
	private long catId;
	private int qty;
	private String country;
	private String alcohol_vol;
	private long orderId;
	private String winebox;
	private String size;
}
