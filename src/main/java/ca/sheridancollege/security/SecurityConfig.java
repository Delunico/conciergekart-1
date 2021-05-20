package ca.sheridancollege.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import ca.sheridancollege.database.DatabaseAccess;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private LoggingAccessDeniedHandler accessDeniedHandler;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	DatabaseAccess da;
	
	@Autowired
	private DataSource dataSource;
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
//	@Bean
//	public JdbcUserDetailsManager jdbcUserDetailsManager() throws Exception {
//		
//		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
//
//		jdbcUserDetailsManager.setDataSource(dataSource);
//		
//		return jdbcUserDetailsManager;
//	}
	
	@Autowired
	@Lazy //gives the controller access to this private data field, shares it.
	private BCryptPasswordEncoder passwordEncoder;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers("/user/**").hasAnyRole("USER","MANAGER")
		.antMatchers("/admin/**").hasRole("MANAGER")
		.antMatchers("/h2-console/**").permitAll()
		.antMatchers("/","/**").permitAll()
		.and()
		.formLogin().loginPage("/login")
		.defaultSuccessUrl("/")
		.and()
		.logout().invalidateHttpSession(true)
		.clearAuthentication(true)
		.and()
		.rememberMe().tokenRepository(persistentTokenRepository())
		.and()
		.exceptionHandling()
		.accessDeniedHandler(accessDeniedHandler);
		
		
		http.csrf().disable();
		http.headers().frameOptions().disable();
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception  {
		auth.userDetailsService(userDetailsService);
	}
	
	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		final JdbcTokenRepositoryImpl jdbcTokenRepositoryImpl = new JdbcTokenRepositoryImpl();
		jdbcTokenRepositoryImpl.setDataSource(dataSource);
		return jdbcTokenRepositoryImpl;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.jdbcAuthentication()
		.dataSource(dataSource)
		.usersByUsernameQuery(
                "select email,password, enabled from user_table where email=?")
        .authoritiesByUsernameQuery(
        		"select userEmail, authority from authorities where userEmail = ?")
//                "select user_table.email,authority from authorities inner join user_table on user_table.user=authorities.userid where email=?")
		.passwordEncoder(passwordEncoder);//auto injected at the top of page
	}
	
	
}
