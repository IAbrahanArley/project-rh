package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.JobApplication;
import br.com.abrahanarley.entities.JobOpening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

	boolean existsByCandidateAndJobOpening(AppUser candidate, JobOpening jobOpening);

	Page<JobApplication> findByCandidate(AppUser candidate, Pageable pageable);

	Page<JobApplication> findByJobOpening(JobOpening jobOpening, Pageable pageable);

	Optional<JobApplication> findByIdAndCandidate(UUID id, AppUser candidate);
}
