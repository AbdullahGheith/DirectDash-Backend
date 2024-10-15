package controllertests;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.mockito.Mockito.*;

import com.directdash.backend.Application;
import com.directdash.backend.model.Location;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.directdash.backend.controllers.SignedInUser;
import com.directdash.backend.controllers.WorkerController;
import com.directdash.backend.model.Work;
import com.directdash.backend.model.Worker;
import com.directdash.backend.model.Job;
import com.directdash.backend.model.JobOffer;
import com.directdash.backend.model.enums.Vehicle;
import com.directdash.backend.model.enums.WorkStatus;
import com.directdash.backend.model.enums.JobStatus;
import com.directdash.backend.model.enums.JobOfferStatus;
import com.directdash.backend.model.repos.WorkRepository;
import com.directdash.backend.model.repos.WorkerRepository;
import com.directdash.backend.model.repos.JobRepository;
import com.directdash.backend.model.repos.LocationRepository;
import com.directdash.backend.model.repos.JobOfferRepository;
import com.directdash.backend.services.WorkerService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@Component
@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class WorkerControllerTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WorkerController workerController;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobOfferRepository jobOfferRepository;

    private Worker testWorker;

    @MockBean
    private SignedInUser mockUser;

    @BeforeEach
    void setUp() {
        when(mockUser.getUsername()).thenReturn("worker@example.com");

        testWorker = new Worker();
        testWorker.setEmail("worker@example.com");
        testWorker.setName("Test Worker");
        entityManager.persist(testWorker);
    }

    @Test
    @Transactional
    void testStartWorking() {
        Work work = workerController.startWorking(10.0, Vehicle.Car, 40.7128, -74.0060).getBody();

        assertNotNull(work);
        assertEquals(WorkStatus.Started, work.getStatus());
        assertEquals(10.0, work.getMaxDistanceKm());
        assertEquals(Vehicle.Car, work.getVehicle());
        assertEquals(40.7128, work.getStartLatitude());
        assertEquals(-74.0060, work.getStartLongitude());

        Work savedWork = workRepository.findById(work.getId()).orElse(null);
        assertNotNull(savedWork);
        assertEquals(work.getId(), savedWork.getId());

        assertEquals(WorkStatus.Started, savedWork.getStatus());
        assertEquals(10.0, savedWork.getMaxDistanceKm());
        assertEquals(Vehicle.Car, savedWork.getVehicle());
        assertEquals(40.7128, savedWork.getStartLatitude());
        assertEquals(-74.0060, savedWork.getStartLongitude());
        // Assert that starting work again throws IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workerController.startWorking(10.0, Vehicle.Car, 40.7128, -74.0060);
        });
        assertEquals("Worker already has an active work session", exception.getMessage());


    }

    @Test
    @Transactional
    void testStopWorking() {
        // Arrange: Start working
        Work startedWork = workerController.startWorking(10.0, Vehicle.Car, 40.7128, -74.0060).getBody();
        assertNotNull(startedWork);
        assertEquals(WorkStatus.Started, startedWork.getStatus());

        // Act & Assert: Stop working successfully
        Work stoppedWork = workerController.stopWorking().getBody();
        assertNotNull(stoppedWork);
        assertEquals(WorkStatus.Finished, stoppedWork.getStatus());
        assertNotNull(stoppedWork.getEndTime());

        // Act & Assert: Attempt to stop working again, expect an exception
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workerController.stopWorking();
        });
        assertEquals("No active work session found for the user", exception.getMessage());
    }

    @Test
    @Transactional
    void testAcceptJobOffer() {
        // Create a job and job offer
        Job job = new Job();
        job.setStatus(JobStatus.Awaiting);
        entityManager.persist(job);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setJob(job);
        jobOffer.setStatus(JobOfferStatus.Pending);
        entityManager.persist(jobOffer);
        entityManager.flush();

        Job acceptedJob = workerController.acceptJobOffer(jobOffer.getId()).getBody();

        assertNotNull(acceptedJob);
        assertEquals(JobStatus.Accepted, acceptedJob.getStatus());

        JobOffer savedJobOffer = jobOfferRepository.findById(jobOffer.getId()).orElse(null);
        assertNotNull(savedJobOffer);
        assertEquals(JobOfferStatus.Accepted, savedJobOffer.getStatus());
    }

    @Test
    @Transactional
    void testRejectJobOffer() {
        // Create a job and job offer
        Job job = new Job();
        job.setStatus(JobStatus.Awaiting);
        entityManager.persist(job);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setJob(job);
        jobOffer.setStatus(JobOfferStatus.Pending);
        entityManager.persist(jobOffer);
        entityManager.flush();

        JobOffer rejectedJobOffer = workerController.rejectJobOffer(jobOffer.getId()).getBody();

        assertNotNull(rejectedJobOffer);
        assertEquals(JobOfferStatus.Rejected, rejectedJobOffer.getStatus());

        //get job and make sure it is still awaiting
        Job savedJob = jobRepository.findById(job.getId()).orElse(null);
        assertEquals(JobStatus.Awaiting, savedJob.getStatus());
    }

    @Test
    @Transactional
    void testReportLocation() {
        // Act & Assert: Report location successfully
        boolean result = workerController.reportLocation(40.7128, -74.0060).getBody();
        assertFalse(result);

        Work work = workerController.startWorking(10D, Vehicle.Car, 40.7128, -74.0060).getBody();
        assertNotNull(work);
        assertEquals(WorkStatus.Started, work.getStatus());

        boolean result2 = workerController.reportLocation(10.0, 10.0).getBody();
        assertTrue(result2);

        boolean result3 = workerController.reportLocation(20.0, 20.0).getBody();
        assertTrue(result3);

        List<Location> all = locationRepository.findAll();
        assertEquals(2, all.size());
        assertEquals(10.0, all.get(0).getLatitude());
        assertEquals(10.0, all.get(0).getLongitude());
        assertEquals(20.0, all.get(1).getLatitude());
        assertEquals(20.0, all.get(1).getLongitude());
    }

    @Test
    @Transactional
    void testMarkOrderAsReceived() {
        // Create a job and job offer
        Job job = new Job();
        job.setStatus(JobStatus.Awaiting);
        entityManager.persist(job);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setJob(job);
        jobOffer.setStatus(JobOfferStatus.Pending);
        entityManager.persist(jobOffer);

        Job receivedJob = workerController.markOrderAsReceived(job.getId()).getBody();

        assertNotNull(receivedJob);
        assertEquals(JobStatus.DeliveredToWorker, receivedJob.getStatus());

        Job savedJob = jobRepository.findById(job.getId()).orElse(null);
        assertEquals(JobStatus.DeliveredToWorker, savedJob.getStatus());

    }
    
    @Test
    @Transactional
    void testMarkOrderAsDelivered() {
        // Create a job and job offer
        Job job = new Job();
        job.setStatus(JobStatus.Accepted);
        entityManager.persist(job);

        JobOffer jobOffer = new JobOffer();
        jobOffer.setJob(job);
        jobOffer.setStatus(JobOfferStatus.Pending);
        entityManager.persist(jobOffer);

        Job deliveredJob = workerController.markOrderAsDelivered(job.getId()).getBody();

        assertNotNull(deliveredJob);
        assertEquals(JobStatus.DeliveredToCustomer, deliveredJob.getStatus());

        Job savedJob = jobRepository.findById(job.getId()).orElse(null);
        assertEquals(JobStatus.DeliveredToCustomer, savedJob.getStatus());
    }
}
