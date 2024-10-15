package com.directdash.backend.model.repos;

import com.directdash.backend.model.Work;
import com.directdash.backend.model.Worker;
import com.directdash.backend.model.enums.WorkStatus;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkRepository extends JpaRepository<Work, String> {
    
    Optional<Work> findTopByWorkerAndStatusOrderByStartTimeDesc(Worker worker, WorkStatus status);

}