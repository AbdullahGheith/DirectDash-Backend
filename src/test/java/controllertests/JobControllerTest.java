package controllertests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.directdash.backend.Application;
import com.directdash.backend.controllers.JobController;
import com.directdash.backend.controllers.SignedInUser;
import com.directdash.backend.model.Job;
import com.directdash.backend.model.JobOffer;
import com.directdash.backend.model.Restaurant;
import com.directdash.backend.model.Worker;
import com.directdash.backend.model.enums.JobOfferStatus;
import com.directdash.backend.model.enums.JobStatus;
import com.directdash.backend.model.enums.Vehicle;
import com.directdash.backend.model.repos.JobOfferRepository;
import com.directdash.backend.model.repos.JobRepository;
import com.directdash.backend.model.repos.LocationRepository;
import com.directdash.backend.model.repos.RestaurantRepository;
import com.directdash.backend.model.repos.WorkRepository;
import com.directdash.backend.model.repos.WorkerRepository;
import com.directdash.backend.services.JobService;
import com.directdash.backend.services.WorkerService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

@Component
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class JobControllerTest {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JobController jobController;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobOfferRepository jobOfferRepository;

	@Autowired
	private WorkerRepository workerRepository;

	@Autowired
	private JobService jobService;

	@Autowired
	private RestaurantRepository restaurantRepository;

	@Autowired
	private WorkerService workerService;

	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private WorkRepository workRepository;

	@MockBean
	private SignedInUser mockUser;

	private Restaurant testRestaurant;

	private Worker worker1;
	private Worker worker2;
	private Worker worker3;

	@BeforeEach
	void setUp() {
		// Mock the signed-in user as a restaurant
		when(mockUser.getUsername()).thenReturn("restaurant@example.com");

		// Create and persist a test restaurant
		testRestaurant = new Restaurant();
		testRestaurant.setEmail("restaurant@example.com");
		testRestaurant.setName("Test Restaurant");
		entityManager.persist(testRestaurant);

		// Create and persist test workers
		worker1 = new Worker();
		worker1.setEmail("worker1@example.com");
		worker1.setName("Worker One");
		entityManager.persist(worker1);

		worker2 = new Worker();
		worker2.setEmail("worker2@example.com");
		worker2.setName("Worker Two");
		entityManager.persist(worker2);

		worker3 = new Worker();
		worker3.setEmail("worker3@example.com");
		worker3.setName("Worker Three");
		entityManager.persist(worker3);

		// Start working sessions for workers with locations
		startWorkingSession(worker1, 5.0, Vehicle.Car, 40.7128, -74.0060); // New York
		startWorkingSession(worker2, 5.0, Vehicle.Bicycle, 34.0522, -118.2437); // Los Angeles
		startWorkingSession(worker3, 5.0, Vehicle.Car, 41.8781, -87.6298); // Chicago

		// Report their current locations
		reportWorkerLocation(worker1, 40.7128, -74.0060); // New York
		reportWorkerLocation(worker2, 34.0522, -118.2437); // Los Angeles
		reportWorkerLocation(worker3, 41.8781, -87.6298); // Chicago
	}

	private void startWorkingSession(Worker worker, Double maxDistanceKm, Vehicle vehicle, Double lat, Double lon) {
		workerService.startWorking(worker.getEmail(),maxDistanceKm, vehicle, lat, lon);
	}

	private void reportWorkerLocation(Worker worker, Double lat, Double lon) {
		workerService.reportLocation(worker.getEmail(), lat, lon);
	}

	@Test
	@Transactional
	void testCreateJobAndAssignClosestWorker() {
		// Arrange: Create job details
		Double jobLatitude = 40.730610; // Near New York
		Double jobLongitude = -73.935242;

		// Act: Restaurant creates a job
		Job job = jobController.createJob(jobLatitude, jobLongitude, "Deliver Food", 20.0).getBody();

		// Assert: Job is created successfully
		assertNotNull(job);
		assertEquals(JobStatus.Awaiting, job.getStatus());
		assertEquals(testRestaurant.getId(), job.getRestaurant().getId());
		assertEquals(jobLatitude, job.getPickupLatitude());
		assertEquals(jobLongitude, job.getPickupLongitude());

		// Assert: A JobOffer is created for the closest worker (worker1)
		List<JobOffer> jobOffers = jobOfferRepository.findByJob(job);
		assertFalse(jobOffers.isEmpty(), "JobOffer should be created");
		JobOffer jobOffer = jobOffers.get(0);
		assertEquals(worker1.getId(), jobOffer.getWorker().getId(), "JobOffer should be assigned to the closest worker");
		assertEquals(JobOfferStatus.Pending, jobOffer.getStatus());

		// Additional Assert: No JobOffers are created for other workers
		assertEquals(1, jobOffers.size(), "Only one JobOffer should be created for the closest worker");
	}

	@Test
	@Transactional
	void testJobAssignmentWhenNoWorkersInitiallyAvailable() {
		// Arrange: No workers are available initially
		// Do not start any work sessions or report locations

		// Act: Restaurant creates a job
		Double jobLatitude = 40.730610; // Near New York
		Double jobLongitude = -73.935242;
		Job job = jobController.createJob(jobLatitude, jobLongitude, "Urgent Delivery", 20.0, null).getBody();

		// Assert: Job is created successfully but no JobOffer is created
		assertNotNull(job);
		assertEquals(JobStatus.Awaiting, job.getStatus());

		List<JobOffer> initialJobOffers = jobOfferRepository.findByJob(job);
		assertTrue(initialJobOffers.isEmpty(), "No JobOffers should be created when no workers are available");

		// Simulate time passing and a worker becoming available
		// Start a working session for worker1
		startWorkingSession(worker1, 10.0, Vehicle.Car, 40.7128, -74.0060); // New York
		reportWorkerLocation(worker1, 40.7128, -74.0060);

		// Simulate the periodic job assignment check
		jobService.assignPendingJobs();

		// Assert: A JobOffer should now be created for worker1
		List<JobOffer> jobOffersAfterWorkerAvailable = jobOfferRepository.findByJob(job);
		assertFalse(jobOffersAfterWorkerAvailable.isEmpty(), "JobOffer should be created when a worker becomes available");
		JobOffer jobOffer = jobOffersAfterWorkerAvailable.get(0);
		assertEquals(worker1.getId(), jobOffer.getWorker().getId(), "JobOffer should be assigned to the available worker");
		assertEquals(JobOfferStatus.Pending, jobOffer.getStatus());
	}

	@Test
	@Transactional
	void testEmailNotificationWhenDeadlineApproaches() {
		// Arrange: No workers are available initially
		// Do not start any work sessions or report locations

		// Act: Restaurant creates a job with a deadline in 10 minutes
		Double jobLatitude = 40.730610;
		Double jobLongitude = -73.935242;
		LocalDateTime deadline = LocalDateTime.now().plusMinutes(10);
		Job job = jobController.createJobWithDeadline(jobLatitude, jobLongitude, "Urgent Delivery", 20.0, deadline, null).getBody();

		// Assert: Job is created successfully but no JobOffer is created
		assertNotNull(job);
		assertEquals(JobStatus.Awaiting, job.getStatus());

		// Simulate time passing until 2 minutes before the deadline
		// For testing purposes, we'll directly manipulate the job's creation time
		job.setCreatedAt(LocalDateTime.now().minusMinutes(8));
		jobRepository.save(job);

		// Simulate the periodic job assignment check
		jobService.assignPendingJobs();

		// Assert: No JobOffer is created because no workers are available
		List<JobOffer> jobOffers = jobOfferRepository.findByJob(job);
		assertTrue(jobOffers.isEmpty(), "No JobOffers should be created when no workers are available");

		// Simulate checking for approaching deadlines
		jobService.checkForApproachingDeadlines();

		// Capture the email sent
		ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
		verify(emailService, times(1)).sendEmail(emailCaptor.capture(), anyString(), anyString());

		// Assert: An email notification was sent to the restaurant
		String recipientEmail = emailCaptor.getValue();
		assertEquals(testRestaurant.getEmail(), recipientEmail, "Email should be sent to the restaurant");
	}

	@Test
	@Transactional
	void testJobOfferAssignmentBasedOnVehicleType() {
		// Arrange: Create a job requiring a car
		Double jobLatitude = 41.730610; // Near Chicago
		Double jobLongitude = -87.935242;

		// Act: Restaurant creates a job
		Job job = jobController.createJob(jobLatitude, jobLongitude, "Deliver Large Package", 50.0, Vehicle.Car).getBody();

		// Assert: JobOffer is assigned to worker3 who has a car
		List<JobOffer> jobOffers = jobOfferRepository.findByJob(job);
		assertFalse(jobOffers.isEmpty(), "JobOffer should be created");
		JobOffer jobOffer = jobOffers.get(0);
		assertEquals(worker3.getId(), jobOffer.getWorker().getId(), "JobOffer should be assigned to worker with a car");
	}

	@Test
	@Transactional
	void testWorkerCanAcceptJobOffer() {
		// Arrange: Create a job and assign to worker1
		Job job = jobController.createJob(40.730610, -73.935242, "Deliver Documents", 15.0).getBody();
		JobOffer jobOffer = jobOfferRepository.findByJob(job).get(0);

		// Mock signed-in user as worker1
		when(mockUser.getUsername()).thenReturn("worker1@example.com");

		// Act: Worker accepts the JobOffer
		Job acceptedJob = jobController.acceptJobOffer(jobOffer.getId()).getBody();

		// Assert: Job status is updated
		assertNotNull(acceptedJob);
		assertEquals(JobStatus.Accepted, acceptedJob.getStatus());

		// Assert: JobOffer status is updated
		JobOffer updatedJobOffer = jobOfferRepository.findById(jobOffer.getId()).orElse(null);
		assertNotNull(updatedJobOffer);
		assertEquals(JobOfferStatus.Accepted, updatedJobOffer.getStatus());
	}

	@Test
	@Transactional
	void testWorkerCanRejectJobOfferAndOfferGoesToNextBestWorker() {
		// Arrange: Create a job near New York
		Job job = jobController.createJob(40.730610, -73.935242, "Deliver Parcel", 25.0).getBody();
		JobOffer jobOffer = jobOfferRepository.findByJob(job).get(0);

		// Mock signed-in user as worker1
		when(mockUser.getUsername()).thenReturn("worker1@example.com");

		// Act: Worker rejects the JobOffer
		JobOffer rejectedJobOffer = jobController.rejectJobOffer(jobOffer.getId()).getBody();

		// Assert: Original JobOffer is rejected
		assertNotNull(rejectedJobOffer);
		assertEquals(JobOfferStatus.Rejected, rejectedJobOffer.getStatus());

		// Assert: New JobOffer is created for the next best worker (if any)
		List<JobOffer> jobOffers = jobOfferRepository.findByJob(job);
		assertEquals(1, jobOffers.size(), "A new JobOffer should be created for the next best worker");
		JobOffer newJobOffer = jobOffers.get(0);
		assertNotEquals(jobOffer.getId(), newJobOffer.getId(), "A new JobOffer should be created");

		// Since worker2 is too far, the job might be unassigned if no suitable worker is found
		// Adjust assertions based on your business logic
	}

	@Test
	@Transactional
	void testJobLifecycleEndToEnd() {
		// Arrange: Restaurant creates a job
		Job job = jobController.createJob(40.730610, -73.935242, "Complete Delivery", 30.0).getBody();

		// Worker accepts the job
		JobOffer jobOffer = jobOfferRepository.findByJob(job).get(0);
		when(mockUser.getUsername()).thenReturn("worker1@example.com");
		jobController.acceptJobOffer(jobOffer.getId());

		// Worker marks order as received
		Job updatedJob = jobController.markOrderAsReceived(job.getId()).getBody();
		assertEquals(JobStatus.DeliveredToWorker, updatedJob.getStatus());

		// Worker marks order as delivered
		updatedJob = jobController.markOrderAsDelivered(job.getId()).getBody();
		assertEquals(JobStatus.DeliveredToCustomer, updatedJob.getStatus());

		// Assert: Job status is completed
		assertEquals(JobStatus.DeliveredToCustomer, jobRepository.findById(job.getId()).get().getStatus());
	}
}
