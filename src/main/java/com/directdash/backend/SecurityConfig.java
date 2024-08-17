package com.directdash.backend;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {


	public SecurityConfig() {

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests()
				.requestMatchers(
						new AntPathRequestMatcher("/signup"),
						new AntPathRequestMatcher("/VAADIN/*"),
						new AntPathRequestMatcher("/images/*"))
				.permitAll()
				.and()
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
				.authorizeHttpRequests()
				.requestMatchers(new AntPathRequestMatcher("/user/**"))
				.authenticated()
				.and()
				.csrf().disable();
		super.configure(http);
		setOAuth2LoginPage(http, "/oauth2/authorization/cognito");
		http.oauth2Login(l -> l.userInfoEndpoint().userAuthoritiesMapper(userAuthoritiesMapper()));
	}



	@Override
	public void configure(WebSecurity web) throws Exception {
		// Customize your WebSecurity configuration.
		super.configure(web);
	}

	@Bean
	public GrantedAuthoritiesMapper userAuthoritiesMapper() {
		return (authorities) -> {

			Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
			authorities.stream().findFirst().ifPresent(oidcUser -> {
				OidcUserAuthority user = (OidcUserAuthority)oidcUser;
				ArrayList<String> groups = (ArrayList<String>) user.getAttributes().get("cognito:groups");
				if (groups != null) {
					mappedAuthorities.addAll(groups.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toSet()));
				}
			});


			return mappedAuthorities;
		};
	}
}
