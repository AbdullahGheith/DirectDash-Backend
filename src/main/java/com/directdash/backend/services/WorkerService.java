package com.directdash.backend.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.directdash.backend.controllers.SignedInUser;
import com.directdash.backend.model.Job;
import com.directdash.backend.model.JobOffer;
import com.directdash.backend.model.Work;
import com.directdash.backend.model.Worker;
import com.directdash.backend.model.enums.JobOfferStatus;
import com.directdash.backend.model.enums.JobStatus;
import com.directdash.backend.model.enums.Vehicle;
import com.directdash.backend.model.enums.WorkStatus;
import com.directdash.backend.model.repos.JobOfferRepository;
import com.directdash.backend.model.repos.JobRepository;
import com.directdash.backend.model.repos.WorkRepository;
import com.directdash.backend.model.repos.WorkerRepository;

@Service
public class WorkerService {

    @Autowired
	private WorkRepository workRepository;
    
    @Autowired
	private WorkerRepository workerRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobOfferRepository jobOfferRepository;

	public Work startWorking(SignedInUser user, Double maxDistance, Vehicle vehicle, Double startLatitude, Double startLongitude) {
		Worker worker = workerRepository.findByEmail(user.getUsername())
			.orElseThrow(() -> new IllegalArgumentException("Worker not found"));

		Optional<Work> existingWork = workRepository.findTopByWorkerAndStatusOrderByStartTimeDesc(worker, WorkStatus.Started);
		if (existingWork.isPresent()) {
			throw new IllegalStateException("Worker already has an active work session");
		}

		Work newWork = new Work();
		newWork.setMaxDistanceKm(maxDistance);
		newWork.setVehicle(vehicle);
		newWork.setStartLatitude(startLatitude);
		newWork.setStartLongitude(startLongitude);
		newWork.setStartTime(LocalDateTime.now());
		newWork.setStatus(WorkStatus.Started);

		worker.addWork(newWork);
		workerRepository.save(worker);

		return workRepository.save(newWork);
	}


	public Work stopWorking(SignedInUser user) {
		Worker worker = workerRepository.findByEmail(user.getUsername())
			.orElseThrow(() -> new IllegalArgumentException("Worker not found"));

		Work latestWork = workRepository.findTopByWorkerAndStatusOrderByStartTimeDesc(worker, WorkStatus.Started)
			.orElseThrow(() -> new IllegalStateException("No active work session found for the user"));

		latestWork.setEndTime(LocalDateTime.now());
		latestWork.setStatus(WorkStatus.Finished);
		return workRepository.save(latestWork);
	}

	public Job acceptJobOffer(SignedInUser user, String jobOfferId) {
		JobOffer jobOffer = jobOfferRepository.findByIdAndWorkerEmail(jobOfferId, user.getUsername())
			.orElseThrow(() -> new IllegalArgumentException("Job offer not found"));
		
		if (jobOffer.getStatus() != JobOfferStatus.Pending) {
			throw new IllegalStateException("Job offer is no longer pending");
		}
		
		jobOffer.setStatus(JobOfferStatus.Accepted);
		jobOfferRepository.save(jobOffer);
		
		Job job = jobOffer.getJob();
		job.setStatus(JobStatus.Accepted);
		return jobRepository.save(job);
	}

	public JobOffer rejectJobOffer(SignedInUser user, String jobOfferId) {
		JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
			.orElseThrow(() -> new IllegalArgumentException("Job offer not found"));
		
		if (jobOffer.getStatus() != JobOfferStatus.Pending) {
			throw new IllegalStateException("Job offer is no longer pending");
		}
		
		jobOffer.setStatus(JobOfferStatus.Rejected);
		return jobOfferRepository.save(jobOffer);
	}

	public boolean reportLocation(SignedInUser user, Double latitude, Double longitude) {
		Worker worker = workerRepository.findByEmail(user.getUsername())
			.orElseThrow(() -> new IllegalArgumentException("Worker not found"));

		worker.addLocation(latitude, longitude);
		workerRepository.save(worker);
		return true;
	}

	public Job markOrderAsReceived(SignedInUser user, String jobId) {
		Job job = jobRepository.findById(jobId)
			.orElseThrow(() -> new IllegalArgumentException("Job not found"));

		job.setStatus(JobStatus.DeliveredToWorker);
		return jobRepository.save(job);
	}

	public Job markOrderAsDelivered(SignedInUser user, String jobId) {
		Job job = jobRepository.findById(jobId)
			.orElseThrow(() -> new IllegalArgumentException("Job not found"));

		job.setStatus(JobStatus.DeliveredToCustomer);
		return jobRepository.save(job);
	}
}
