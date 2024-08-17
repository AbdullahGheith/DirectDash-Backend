package com.directdash.backend.model;

import com.directdash.backend.model.enums.Vehicle;
import com.directdash.backend.model.enums.WorkStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

@Entity
public class Work extends DBObject{

	@Enumerated(EnumType.STRING)
	public WorkStatus status;
	public LocalDateTime startTime;
	public LocalDateTime endTime;
	@Enumerated(EnumType.STRING)
	public Vehicle vehicle;
	public Double maxDistanceKm;

	public WorkStatus getStatus() {
		return status;
	}

	public void setStatus(WorkStatus status) {
		this.status = status;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Double getMaxDistanceKm() {
		return maxDistanceKm;
	}

	public void setMaxDistanceKm(Double maxDistanceKm) {
		this.maxDistanceKm = maxDistanceKm;
	}
}
