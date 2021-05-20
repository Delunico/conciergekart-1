package ca.sheridancollege.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Review {
	
	private long id;
	private long productId;
	private String text;
	private double stars;
	private String userEmail;
	private long userId;
}
