package com.directdash.backend.model.repos;

import com.directdash.backend.model.JobOffer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOfferRepository extends JpaRepository<JobOffer, String> {

    Optional<JobOffer> findByWorkerEmailAndId(String email, String id);
}