package com.directdash.backend.controllers;

import com.directdash.backend.services.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import com.directdash.backend.model.Job;
import com.directdash.backend.model.JobOffer;
import com.directdash.backend.model.Work;
import com.directdash.backend.model.enums.Vehicle;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkerController {

	@Autowired
	private WorkerService workerService;

	@Autowired
	SignedInUser user;

	@PostMapping("/worker/start")
	public ResponseEntity<Work> startWorking(@RequestParam Double maxDistance, @RequestParam Vehicle vehicle, @RequestParam Double startLatitude, @RequestParam Double startLongitude) {
		Work work = workerService.startWorking(user, maxDistance, vehicle, startLatitude, startLongitude);
		return ResponseEntity.ok(work);
	}

	@PostMapping("/worker/stop")
	public ResponseEntity<Work> stopWorking() {
		Work work = workerService.stopWorking(user);
		return ResponseEntity.ok(work);
	}

	@PostMapping("/worker/acceptjob")
	public ResponseEntity<Job> acceptJobOffer(@RequestParam String jobOfferId) {
		Job acceptedJob = workerService.acceptJobOffer(user, jobOfferId);
		return ResponseEntity.ok(acceptedJob);
	}

	@PostMapping("/worker/rejectjob")
	public ResponseEntity<JobOffer> rejectJobOffer(@RequestParam String jobOfferId) {
		JobOffer rejectedJobOffer = workerService.rejectJobOffer(user, jobOfferId);
		return ResponseEntity.ok(rejectedJobOffer);
	}

	@PostMapping("/worker/reportlocation")
	public ResponseEntity<Boolean> reportLocation(@RequestParam Double latitude, @RequestParam Double longitude) {
		boolean success = workerService.reportLocation(user, latitude, longitude);
		return ResponseEntity.ok(success);
	}

	@PostMapping("/worker/markreceived")
	public ResponseEntity<Job> markOrderAsReceived(@RequestParam String jobId) {
		Job updatedJob = workerService.markOrderAsReceived(user, jobId);
		return ResponseEntity.ok(updatedJob);
	}

	@PostMapping("/worker/markdelivered")
	public ResponseEntity<Job> markOrderAsDelivered(@RequestParam String jobId) {
		Job completedJob = workerService.markOrderAsDelivered(user, jobId);
		return ResponseEntity.ok(completedJob);
	}
	

}
