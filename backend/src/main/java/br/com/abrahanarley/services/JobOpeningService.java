package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.JobOpeningFilter;
import br.com.abrahanarley.dto.request.JobOpeningRequest;
import br.com.abrahanarley.dto.response.JobOpeningResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.JobOpeningRepository;
import br.com.abrahanarley.repositories.specifications.JobOpeningSpecifications;
import br.com.abrahanarley.security.AuthenticatedUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobOpeningService {

	private final JobOpeningRepository jobOpeningRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public JobOpeningService(JobOpeningRepository jobOpeningRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.jobOpeningRepository = jobOpeningRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional(readOnly = true)
	public Page<JobOpeningResponse> list(JobOpeningFilter filter, Pageable pageable) {
		Page<JobOpening> jobs = jobOpeningRepository.findAll(JobOpeningSpecifications.matching(filter), pageable);

		return jobs.map(JobOpeningResponse::from);
	}

	@Transactional(readOnly = true)
	public JobOpeningResponse findById(UUID id) {
		return JobOpeningResponse.from(getById(id));
	}

	@Transactional
	public JobOpeningResponse create(JobOpeningRequest request) {
		AppUser currentUser = authenticatedUserService.currentUser();
		JobOpening job = new JobOpening(
				request.title(),
				request.description(),
				request.requirements(),
				request.department(),
				request.location(),
				currentUser);
		job.update(request.title(), request.description(), request.requirements(), request.department(),
				request.location(), request.status());

		return JobOpeningResponse.from(jobOpeningRepository.save(job));
	}

	@Transactional
	public JobOpeningResponse update(UUID id, JobOpeningRequest request) {
		JobOpening job = getById(id);
		job.update(request.title(), request.description(), request.requirements(), request.department(),
				request.location(), request.status());
		return JobOpeningResponse.from(job);
	}

	@Transactional
	public void cancel(UUID id) {
		JobOpening job = getById(id);
		job.cancel();
	}

	@Transactional(readOnly = true)
	public JobOpening getById(UUID id) {
		return jobOpeningRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Job opening not found."));
	}
}
