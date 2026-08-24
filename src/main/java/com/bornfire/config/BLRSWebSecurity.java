package com.bornfire.config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
//import java.util.Date;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BGLSAuditTable;
import com.bornfire.entities.BGLSAuditTable_Rep;
import com.bornfire.entities.BGLS_CONTROL_TABLE_REP;
import com.bornfire.entities.BGLS_Control_Table;
import com.bornfire.entities.BLRS_UserProfile_Entity;
import com.bornfire.entities.BLRS_UserProfile_Repo;
import com.bornfire.entities.Access_Role_Repo;
import com.bornfire.entities.Access_Role_Entity;
import com.bornfire.services.LoginServices;

@Configuration
@EnableWebSecurity
public class BLRSWebSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	BLRS_UserProfile_Repo userProfileRep;

	@Autowired
	Access_Role_Repo access_Role_Repo;

	@Autowired
	BGLSAuditTable_Rep bGLSAuditTable_Rep;

	@Autowired
	SequenceGenerator sequence;

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	LoginServices loginServices;

	@Autowired
	PasswordEncryption passwordEncryption;

	@Autowired
	Environment env;

	@Autowired
	BGLS_CONTROL_TABLE_REP bGLS_CONTROL_TABLE_REP;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/webjars/**", "/images/**", "/login*", "/RecievingMail*", "/associatedetails",
						"/changePasswordReq", "/changePasswordLogin", "/freezeColumn/**", "favicon.ico",
						"/rest_password")
				.permitAll().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll()
				.failureHandler(blrsAuthFailHandle()).successHandler(blrsAuthSuccessHandle())
				.usernameParameter("userid").and().logout().permitAll().and().logout()
				.logoutSuccessHandler(xbrlLogoutSuccessHandler()).permitAll().and().sessionManagement()
				.maximumSessions(3).maxSessionsPreventsLogin(false);
		http.csrf().disable();
		http.headers().frameOptions().sameOrigin();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Bean

	public AuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider ap = new DaoAuthenticationProvider() {

			@Override
			@Transactional
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				String userid = authentication.getName();
				String password = authentication.getCredentials().toString();

				Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(userid);

				try {

					if (up.isPresent()) {
						BLRS_UserProfile_Entity usr = up.get();

						// System.out.println("Inside---->"+usr.isAccountNonExpired());
						// System.out.println("isCredentialsNonExpired---->"+usr.isCredentialsNonExpired());
						// System.out.println("isAccountNonLocked---->"+usr.isAccountNonLocked());
						// System.out.println("isEnabled---->"+usr.isEnabled());
						// System.out.println("isLoginAllowed---->"+usr.isLoginAllowed());

						if (!usr.isAccountNonExpired()) {

							throw new AccountExpiredException("Account Expired");

						} else if (!usr.isCredentialsNonExpired()) {

							throw new CredentialsExpiredException("Credentials Expired");

						} else if (!usr.isAccountNonLocked()) {

							throw new LockedException("Account Locked");

						} else if (!usr.isEnabled()) {

							throw new DisabledException("Account Disabled");

						}
						/*
						 * else if (!usr.isLoginAllowed()) {
						 * 
						 * throw new LockedException("Login Not Allowed");
						 * 
						 * }
						 */ 
						boolean passwordValid = AES.validatePassword(password, usr.getPassword());

						if (!passwordValid) {
							Session hs = sessionFactory.getCurrentSession();

							Transaction tr = hs.getTransaction();

							hs.createQuery(
									"update BLRS_UserProfile_Entity a " + "set a.no_of_attmp=nvl(a.no_of_attmp,0)+1, "
											+ "a.user_locked_flg=decode(nvl(a.no_of_attmp,0)+1,'3','Y','N'), "
											+ "a.login_status=decode(nvl(a.no_of_attmp,0)+1,'3','Inactive','Active') "
											+ "where userid=?1")
									.setParameter(1, userid).executeUpdate();

							tr.commit();
							hs.close();

							throw new BadCredentialsException("Authentication failed");
						} else {

							return new UsernamePasswordAuthenticationToken(userid, password, Collections.emptyList());

						}

					} else {

						throw new UsernameNotFoundException("Invalid User Name");
					}

				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					e.printStackTrace();
					authentication.setAuthenticated(false);
				}
				return authentication;

			}

			@Override
			public boolean supports(Class<?> aClass) {
				return aClass.equals(UsernamePasswordAuthenticationToken.class);
			}

		};

		ap.setHideUserNotFoundExceptions(false);
		ap.setUserDetailsService(userDetailsService());

		return ap;

	}

	@Bean
	public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
		return new org.springframework.security.web.session.HttpSessionEventPublisher();
	}

	@Bean
	public AuthenticationFailureHandler blrsAuthFailHandle() {
		return new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {

				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				// logger.info(exception.getMessage());
				response.sendRedirect("login?error=" + exception.getMessage());

			}

		};

	}

	@Bean
	public AuthenticationSuccessHandler blrsAuthSuccessHandle() {
		return new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				String auditID = sequence.generateRequestUUId();
				Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(authentication.getName());

				BGLS_Control_Table up1 = bGLS_CONTROL_TABLE_REP.getTranDate();
				System.out.println(up1.getTran_date());

				BLRS_UserProfile_Entity user = up.get();
				userProfileRep.save(user);

				request.getSession().setAttribute("TRANDATE", up1.getTran_date());
				request.getSession().setAttribute("USERID", user.getUserid());
				request.getSession().setAttribute("USERNAME", user.getUsername());
				request.getSession().setAttribute("BRANCH_ID", user.getBranch_code());
				request.getSession().setAttribute("BRANCH_DESC", user.getBranch_name());
				request.getSession().setAttribute("ROLEID", "");

				// request.getSession().setAttribute("permissions", access_role);
				/* request.getSession().setAttribute("PERMISSIONS", user.getPermissions()); */
				/* request.getSession().setAttribute("WORKCLASS", user.getWork_class()); */
				request.getSession().setAttribute("PASSWORD", user.getPassword());
				request.getSession().setAttribute("LOGIN_TIME",
						LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

				BGLSAuditTable audit = new BGLSAuditTable();
				LocalDateTime currentDateTime = LocalDateTime.now();
				Date dateValue = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
				audit.setAudit_date(new Date());
				audit.setEntry_time(dateValue);
				audit.setEntry_user(user.getUserid());
				audit.setFunc_code("LOGIN");
				audit.setRemarks("Login Successfully");
				audit.setAudit_table("BGLSUSERPROFILE");
				audit.setAudit_screen("LOGIN");
				audit.setEvent_id(user.getUserid());
				audit.setEvent_name(user.getUsername());
				audit.setModi_details("Login Successfully");
				BLRS_UserProfile_Entity auth_user = userProfileRep.getRole(user.getUserid());
				String auth_user_val = auth_user.getAuth_user();
				Date auth_user_date = auth_user.getAuth_time();
				audit.setAuth_user(auth_user_val);
				audit.setAuth_time(auth_user_date);
				audit.setAudit_ref_no(auditID.toString());
				bGLSAuditTable_Rep.save(audit);

				response.sendRedirect("Dashboard");
			}

		};

	}

	@Bean
	public LogoutSuccessHandler xbrlLogoutSuccessHandler() {

		return new LogoutSuccessHandler() {

			@Override
			public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				Optional<BLRS_UserProfile_Entity> up = userProfileRep.findById(authentication.getName());

				BLRS_UserProfile_Entity user = up.get();
				BGLSAuditTable audit = new BGLSAuditTable();
				String Number1 = sequence.generateRequestUUId();
				audit.setAudit_date(new Date());
				audit.setEntry_time(new Date());
				audit.setEntry_user(user.getUserid());
				audit.setFunc_code("LOGOUT");
				audit.setRemarks("Logout Successfully");
				audit.setAudit_table("BGLSUSERPROFILE");
				audit.setAudit_screen("LOGOUT");
				audit.setEvent_id(user.getUserid());
				audit.setEvent_name(user.getUsername());
				BLRS_UserProfile_Entity auth_user = userProfileRep.getRole(user.getUserid());
				String auth_user_val = auth_user.getAuth_user();
				Date auth_user_date = auth_user.getAuth_time();
				audit.setAuth_user(auth_user_val);
				audit.setAuth_time(auth_user_date);
				audit.setModi_details("Logout Successfully");
				audit.setAudit_ref_no(Number1.toString());
				bGLSAuditTable_Rep.save(audit);
				response.sendRedirect("login?logout");
			}
		};
	}

}