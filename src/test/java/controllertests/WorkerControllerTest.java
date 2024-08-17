package controllertests;


import static org.junit.jupiter.api.Assertions.*;

import com.directdash.backend.controllers.WorkerController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkerControllerTest {

	private WorkerController workerController;

	@BeforeEach
	public void setUp() {
		workerController = new WorkerController();
	}

	@Test
	public void startWorking(){
		assertAll(
				() -> assertTrue(workerController.startWorking().getBody()), //work started
				() -> assertFalse(workerController.startWorking().getBody()) //work already started, cant start again
		);
	}


}
