package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CandidateResumeRepository extends JpaRepository<CandidateResume, UUID> {

	Optional<CandidateResume> findByCandidate(AppUser candidate);

	Optional<CandidateResume> findByCandidateId(UUID candidateId);
}
