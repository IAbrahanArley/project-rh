package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobOpeningRepository extends JpaRepository<JobOpening, UUID> {

	Page<JobOpening> findByStatus(JobStatus status, Pageable pageable);

	Page<JobOpening> findAll(Pageable pageable);
}
