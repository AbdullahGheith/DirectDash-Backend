package com.directdash.backend.controllers;

import com.directdash.backend.model.Job;
import com.directdash.backend.model.JobOffer;
import com.directdash.backend.model.Work;
import com.directdash.backend.model.enums.Vehicle;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
public class ShopController {

	@Autowired
	private WorkerController workerController;


	public ResponseEntity<Job> requestDriver(ZonedDateTime timeNeeded, String address) {
		return ResponseEntity.noContent().build();
	}

	public ResponseEntity<Job> markAsReadyForPickup(String jobId) {
		return ResponseEntity.noContent().build();
	}

	public ResponseEntity<Job> getJobStatus(String jobId) {
		return ResponseEntity.noContent().build();
	}

	public ResponseEntity<Job> rateDriver(String jobId, Integer qualityRating, Integer speedRating) {
		return ResponseEntity.noContent().build();
	}

}
