package com.directdash.backend.services;

import com.directdash.backend.controllers.SignedInUser;
import com.directdash.backend.model.Job;
import com.directdash.backend.model.JobOffer;
import com.directdash.backend.model.Location;
import com.directdash.backend.model.Restaurant;
import com.directdash.backend.model.Work;
import com.directdash.backend.model.Worker;
import com.directdash.backend.model.enums.JobOfferStatus;
import com.directdash.backend.model.enums.JobStatus;
import com.directdash.backend.model.enums.Vehicle;
import com.directdash.backend.model.enums.WorkStatus;
import com.directdash.backend.model.repos.JobOfferRepository;
import com.directdash.backend.model.repos.JobRepository;
import com.directdash.backend.model.repos.LocationRepository;
import com.directdash.backend.model.repos.RestaurantRepository;
import com.directdash.backend.model.repos.WorkRepository;
import com.directdash.backend.model.repos.WorkerRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class JobService {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private WorkerRepository workerRepository;

	@Autowired
	private JobOfferRepository jobOfferRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private WorkRepository workRepository;

	@Autowired
	private LocationRepository locationRepository;

	@Scheduled(fixedRate = 60000) // Every minute
	public void scheduledJobAssignment() {
		assignPendingJobs();
	}

	@Scheduled(fixedRate = 60000) // Every minute
	public void scheduledDeadlineCheck() {
		checkForApproachingDeadlines();
	}

	public Job createJob(SignedInUser user, Double pickupLatitude, Double pickupLongitude, String description, Double payAmount, Vehicle requiredVehicle) {
		// Verify that the user is a restaurant
		Restaurant restaurant = restaurantRepository.findByEmail(user.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("User is not a registered restaurant"));

		// Create and save the new job
		Job job = new Job();
		job.setRestaurant(restaurant);
		job.setPickupLatitude(pickupLatitude);
		job.setPickupLongitude(pickupLongitude);
		job.setDescription(description);
		job.setPayAmount(payAmount);
		job.setRequiredVehicle(requiredVehicle);
		job.setStatus(JobStatus.Awaiting);
		jobRepository.save(job);

		// Find the best-suited worker
		Worker bestWorker = findBestWorker(pickupLatitude, pickupLongitude, requiredVehicle);

		if (bestWorker != null) {
			// Create and save a JobOffer for the best worker
			JobOffer jobOffer = new JobOffer();
			jobOffer.setJob(job);
			jobOffer.setWorker(bestWorker);
			jobOffer.setStatus(JobOfferStatus.Pending);
			jobOfferRepository.save(jobOffer);
		}

		return job;
	}

	private Worker findBestWorker(Double pickupLatitude, Double pickupLongitude, Vehicle requiredVehicle) {
		// Find all active workers
		List<Work> activeWorks = workRepository.findByStatus(WorkStatus.Working);

		Worker bestWorker = null;
		double closestDistance = Double.MAX_VALUE;

		for (Work work : activeWorks) {
			// Check vehicle requirements
			if (requiredVehicle != null && !work.getVehicle().equals(requiredVehicle)) {
				continue;
			}

			// Get the latest location of the worker
			Location location = locationRepository.findTopByWorkerOrderByTimestampDesc(work.getWorker());

			if (location != null) {
				double distance = calculateDistance(pickupLatitude, pickupLongitude, location.getLatitude(), location.getLongitude());

				// Check if within worker's max distance
				if (distance <= work.getMaxDistanceKm() && distance < closestDistance) {
					closestDistance = distance;
					bestWorker = work.getWorker();
				}
			}
		}

		return bestWorker;
	}

	private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		// Implement Haversine formula or use a library to calculate distance between two coordinates
		// For simplicity, let's assume this method returns the distance in kilometers
		// ...

		return 0; // Replace with actual calculation
	}
}
