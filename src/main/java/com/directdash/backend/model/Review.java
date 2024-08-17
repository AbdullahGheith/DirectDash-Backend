package com.directdash.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

@Entity
public class Review extends DBObject {

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Shop giver;
	public Integer qualityRating;
	public Integer speedRating;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Job forJob;

}
