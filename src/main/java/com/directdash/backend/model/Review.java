package com.directdash.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Review extends DBObject {

	@ManyToOne
	@JoinColumn(name = "worker_id")
	private Worker worker;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Shop giver;
	public Integer qualityRating;
	public Integer speedRating;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	public Job forJob;

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	public Shop getGiver() {
		return giver;
	}

	public void setGiver(Shop giver) {
		this.giver = giver;
	}

	public Integer getQualityRating() {
		return qualityRating;
	}

	public void setQualityRating(Integer qualityRating) {
		this.qualityRating = qualityRating;
	}

	public Integer getSpeedRating() {
		return speedRating;
	}

	public void setSpeedRating(Integer speedRating) {
		this.speedRating = speedRating;
	}

	public Job getForJob() {
		return forJob;
	}

	public void setForJob(Job forJob) {
		this.forJob = forJob;
	}
}
