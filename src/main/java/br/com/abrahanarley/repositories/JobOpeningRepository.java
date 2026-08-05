package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobOpeningRepository extends JpaRepository<JobOpening, UUID> {

	List<JobOpening> findByStatusOrderByCreatedAtDesc(JobStatus status);

	List<JobOpening> findAllByOrderByCreatedAtDesc();
}
