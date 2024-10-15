package com.directdash.backend.model.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.directdash.backend.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {
    
}
