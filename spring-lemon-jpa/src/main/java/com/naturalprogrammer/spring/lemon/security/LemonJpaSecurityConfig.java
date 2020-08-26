package com.naturalprogrammer.spring.lemon.security;

import com.naturalprogrammer.spring.lemon.commons.LemonProperties;
import com.naturalprogrammer.spring.lemon.commonsweb.security.LemonWebSecurityConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class. Extend it in the
 * application, and make a configuration class. Override
 * protected methods, if you need any customization.
 * 
 * @author Sanjay Patel
 */
public class LemonJpaSecurityConfig extends LemonWebSecurityConfig {
	
	private static final Log log = LogFactory.getLog(LemonJpaSecurityConfig.class);

	private LemonProperties properties;
	private LemonUserDetailsService<?, ?> userDetailsService;
	private LemonAuthenticationSuccessHandler authenticationSuccessHandler;
	private LemonOidcUserService oidcUserService;
	private LemonOAuth2UserService<?, ?> oauth2UserService;
	private OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
	private OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;
	
	@Autowired
	public void createLemonSecurityConfig(LemonProperties properties, LemonUserDetailsService<?, ?> userDetailsService,
			LemonAuthenticationSuccessHandler authenticationSuccessHandler,
			LemonOidcUserService oidcUserService,
			LemonOAuth2UserService<?, ?> oauth2UserService,
			OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler,
			OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler) {

		this.properties = properties;
		this.userDetailsService = userDetailsService;
		this.authenticationSuccessHandler = authenticationSuccessHandler;
		this.oidcUserService = oidcUserService;
		this.oauth2UserService = oauth2UserService;
		this.oauth2AuthenticationSuccessHandler = oauth2AuthenticationSuccessHandler;
		this.oauth2AuthenticationFailureHandler = oauth2AuthenticationFailureHandler;
		
		log.info("Created");
	}

	/**
	 * Security configuration, calling protected methods
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		super.configure(http);
		login(http); // authentication
		exceptionHandling(http); // exception handling
		oauth2Client(http);
	}

	
	/**
	 * Configuring authentication.
	 */
	protected void login(HttpSecurity http) throws Exception {
		
		http
		.formLogin() // form login
			.loginPage(loginPage())
			
			/******************************************
			 * Setting a successUrl would redirect the user there. Instead,
			 * let's send 200 and the userDto along with an Authorization token.
			 *****************************************/
			.successHandler(authenticationSuccessHandler)
			
			/*******************************************
			 * Setting the failureUrl will redirect the user to
			 * that url if login fails. Instead, we need to send
			 * 401. So, let's set failureHandler instead.
			 *******************************************/
        	.failureHandler(new SimpleUrlAuthenticationFailureHandler());
	}

	
	/**
	 * Override this to change login URL
	 * 
	 * @return
	 */
	protected String loginPage() {
		
		return properties.getLoginUrl();
	}

	
	protected void oauth2Client(HttpSecurity http) throws Exception {
		
		http.oauth2Login()
			.authorizationEndpoint()
				.authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository(properties)).and()
			.successHandler(oauth2AuthenticationSuccessHandler)
			.failureHandler(oauth2AuthenticationFailureHandler)
			.userInfoEndpoint()
				.oidcUserService(oidcUserService)
				.userService(oauth2UserService);
	}	

	
	/**
	 * Configuring token authentication filter
	 */
	@Override
	protected void tokenAuthentication(HttpSecurity http) {
		
		http.addFilterBefore(new LemonJpaTokenAuthenticationFilter(blueTokenService, userDetailsService),
				UsernamePasswordAuthenticationFilter.class);
	}
}
