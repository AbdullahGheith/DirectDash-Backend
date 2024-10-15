package com.directdash.backend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(unique = true, nullable = false)
	private String email;

	private String name;
	private String address;
	private String phoneNumber;

	@OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
	private List<Job> jobs;

	// Constructors

	public Restaurant() {
		// Default constructor required by JPA
	}

	public Restaurant(String email, String name, String address, String phoneNumber) {
		this.email = email;
		this.name = name;
		this.address = address;
		this.phoneNumber = phoneNumber;
	}

	// Getters and Setters

	public String getId() {
		return id;
	}

	// Email

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// Name

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// Address

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	// Phone Number

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	// Jobs

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
}
