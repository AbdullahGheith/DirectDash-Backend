package com.directdash.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class Job extends DBObject {

	public String title;
	public String destinationAddress;
	public Integer destinationZipCode;
	@Column(length = 2000)
	public String comment;
	public LocalDateTime creationDate;
	public Integer neededInMinutes;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public Integer getDestinationZipCode() {
		return destinationZipCode;
	}

	public void setDestinationZipCode(Integer destinationZipCode) {
		this.destinationZipCode = destinationZipCode;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public Integer getNeededInMinutes() {
		return neededInMinutes;
	}

	public void setNeededInMinutes(Integer neededInMinutes) {
		this.neededInMinutes = neededInMinutes;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
