package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.JobOpening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JobOpeningRepository extends JpaRepository<JobOpening, UUID>, JpaSpecificationExecutor<JobOpening> {
}
