package com.directdash.backend.model.repos;

import com.directdash.backend.model.Worker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, String> {

	Optional<Worker> findByEmail(String email);

}