package com.directdash.backend.controllers;

import com.directdash.backend.model.Job;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
public class WorkerController {

	@GetMapping(value="/test")
	public ResponseEntity<Boolean> startWorking() {
		return ResponseEntity.ok("Hello World");
	}

	public ResponseEntity<String> stopWorking() {

	}

	public ResponseEntity<List<Job>> checkWorkerOffers() {

	}

}
