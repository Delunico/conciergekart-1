package ca.sheridancollege.beans;

import lombok.Data;

@Data
public class Category {

	private String title;
	private Long id;
	
	@Override
	public String toString() {
		return "Category [title=" + title + ", id=" + id + "]";
	}
	
	
}
