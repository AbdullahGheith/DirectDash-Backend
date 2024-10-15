package com.directdash.backend.controllers;

import com.directdash.backend.model.Job;
import com.directdash.backend.model.enums.Vehicle;
import com.directdash.backend.services.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling job-related operations initiated by restaurants.
 */
@RestController
public class JobController {

	@Autowired
	private JobService jobService;

	@Autowired
	private SignedInUser user;

	/**
	 * Endpoint for restaurants to create a new job.
	 *
	 * @param pickupLatitude  Latitude of the pickup location.
	 * @param pickupLongitude Longitude of the pickup location.
	 * @param description     Description of the job.
	 * @param payAmount       Payment amount for the job.
	 * @param requiredVehicle (Optional) Required vehicle type for the job.
	 * @return The created Job object.
	 */
	@PostMapping("/job/create")
	public ResponseEntity<Job> createJob(
			@RequestParam Double pickupLatitude,
			@RequestParam Double pickupLongitude,
			@RequestParam String description,
			@RequestParam Double payAmount,
			@RequestParam(required = false) Vehicle requiredVehicle) {

		Job job = jobService.createJob(
				user,
				pickupLatitude,
				pickupLongitude,
				description,
				payAmount,
				requiredVehicle);

		return ResponseEntity.ok(job);
	}
}
