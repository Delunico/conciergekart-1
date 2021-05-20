package ca.sheridancollege.beans;

import lombok.Data;

@Data
public class Orders {

	private int id;		    
	private String userEmail;	
	private int status; 		
	private double total;		
	private String streetAddress; 
	private String timePlaced; 
	private String sessionId;	
	private int driverId = 0;
}
