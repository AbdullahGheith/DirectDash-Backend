package com.directdash.backend.model;

import com.directdash.backend.model.enums.JobOfferStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class JobOffer extends DBObject {

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Job job;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Worker worker;
	@Enumerated(EnumType.STRING)
	public JobOfferStatus status;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	public JobOfferStatus getStatus() {
		return status;
	}

	public void setStatus(JobOfferStatus status) {
		this.status = status;
	}
}
