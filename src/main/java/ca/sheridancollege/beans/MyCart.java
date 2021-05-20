package ca.sheridancollege.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MyCart {
	
	private Long id;
	private long userId;
	private int status;
	private String sessionId;
}
