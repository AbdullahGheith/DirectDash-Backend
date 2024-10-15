package com.directdash.backend;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
				.authorizeHttpRequests(authz -> authz
						.requestMatchers(
								new AntPathRequestMatcher("/signup"),
								new AntPathRequestMatcher("/VAADIN/**"),
								new AntPathRequestMatcher("/images/**"),
								new AntPathRequestMatcher("/oauth2/authorization/cognito")
						).permitAll()
						.requestMatchers(AntPathRequestMatcher.antMatcher("/**")).authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.jwtAuthenticationConverter(new JwtAuthenticationConverter())
						)
				)
				.csrf(AbstractHttpConfigurer::disable);

		super.configure(http);
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withJwkSetUri("https://cognito-idp.eu-west-1.amazonaws.com/eu-west-1_geJUikPA0/.well-known/jwks.json").build();
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
