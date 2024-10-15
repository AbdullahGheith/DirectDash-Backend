package com.directdash.backend.controllers;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SignedInUser {
	public String getUsername(){
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		if (email.contains("@")){
			return email.toLowerCase();
		} else {
			email = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaim("email");
			if (email.contains("@")){
				return email.toLowerCase();
			}
		}
		throw new RuntimeException("Cognito error? Token received but doesnt contain email!");
	}

	public boolean isAnonymous(){
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser") || SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymous");
	}

	public boolean isAdmin(){
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
	}
	public boolean isMobile(){
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_mobile"));
	}
}
