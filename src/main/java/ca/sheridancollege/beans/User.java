package ca.sheridancollege.beans;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {

	private long id;
	private String email;
	private String f_name;
	private String l_name;
	private String password;
	private String phone;
	private int birthYear;
	private List<GrantedAuthority> authorities;
	
	public User(String f_name,String l_name, String password, String email, String phone, int birthYear, List<GrantedAuthority> authorities) {
		super();
		this.f_name = f_name;
		this.l_name = l_name;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.birthYear = birthYear;
		this.authorities = authorities;
	}
	
	
	

}
