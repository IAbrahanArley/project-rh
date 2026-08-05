package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobOpeningRepository extends JpaRepository<JobOpening, Long> {

	List<JobOpening> findByStatusOrderByCreatedAtDesc(JobStatus status);

	List<JobOpening> findAllByOrderByCreatedAtDesc();
}
