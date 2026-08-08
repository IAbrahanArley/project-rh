package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.CandidateEvaluation;
import br.com.abrahanarley.entities.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidateEvaluationRepository extends JpaRepository<CandidateEvaluation, UUID> {

	Optional<CandidateEvaluation> findByApplication(JobApplication application);
}
