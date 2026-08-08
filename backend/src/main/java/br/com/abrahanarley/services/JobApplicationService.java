package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.ApplicationStatusRequest;
import br.com.abrahanarley.dto.request.EvaluationRequest;
import br.com.abrahanarley.dto.request.JobApplicationRequest;
import br.com.abrahanarley.dto.response.EvaluationResponse;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.CandidateEvaluation;
import br.com.abrahanarley.entities.JobApplication;
import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.enums.JobStatus;
import br.com.abrahanarley.exceptions.BusinessException;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.CandidateEvaluationRepository;
import br.com.abrahanarley.repositories.JobApplicationRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobApplicationService {

	private final JobApplicationRepository jobApplicationRepository;
	private final CandidateEvaluationRepository candidateEvaluationRepository;
	private final JobOpeningService jobOpeningService;
	private final AuthenticatedUserService authenticatedUserService;
	private final NotificationService notificationService;

	public JobApplicationService(JobApplicationRepository jobApplicationRepository,
			CandidateEvaluationRepository candidateEvaluationRepository,
			JobOpeningService jobOpeningService,
			AuthenticatedUserService authenticatedUserService,
			NotificationService notificationService) {
		this.jobApplicationRepository = jobApplicationRepository;
		this.candidateEvaluationRepository = candidateEvaluationRepository;
		this.jobOpeningService = jobOpeningService;
		this.authenticatedUserService = authenticatedUserService;
		this.notificationService = notificationService;
	}

	@Transactional
	public JobApplicationResponse apply(UUID jobId, JobApplicationRequest request) {
		AppUser candidate = authenticatedUserService.currentUser();
		JobOpening job = jobOpeningService.getById(jobId);

		if (job.getStatus() != JobStatus.OPEN) {
			throw new BusinessException("Only open job positions can receive applications.");
		}

		if (jobApplicationRepository.existsByCandidateAndJobOpening(candidate, job)) {
			throw new BusinessException("Candidate already applied to this job opening.");
		}

		JobApplication application = jobApplicationRepository.save(new JobApplication(candidate, job,
				request.motivation()));

		notificationService.notify(candidate, "Candidatura recebida",
				"Sua candidatura para " + job.getTitle() + " foi registrada.");
		notificationService.notify(job.getCreatedBy(), "Nova candidatura",
				candidate.getFullName() + " candidatou-se para " + job.getTitle() + ".");

		return JobApplicationResponse.from(application);
	}

	@Transactional(readOnly = true)
	public Page<JobApplicationResponse> listMine(Pageable pageable) {
		AppUser candidate = authenticatedUserService.currentUser();
		return jobApplicationRepository.findByCandidate(candidate, pageable)
				.map(JobApplicationResponse::from);
	}

	@Transactional(readOnly = true)
	public Page<JobApplicationResponse> listByJob(UUID jobId, Pageable pageable) {
		JobOpening job = jobOpeningService.getById(jobId);
		return jobApplicationRepository.findByJobOpening(job, pageable)
				.map(JobApplicationResponse::from);
	}

	@Transactional
	public JobApplicationResponse updateStatus(UUID applicationId, ApplicationStatusRequest request) {
		JobApplication application = getById(applicationId);
		application.updateStatus(request.status(), request.feedback());

		notificationService.notify(application.getCandidate(), "Status da candidatura atualizado",
				"Sua candidatura para " + application.getJobOpening().getTitle() + " agora estÃ¡ como "
						+ request.status() + ".");

		return JobApplicationResponse.from(application);
	}

	@Transactional
	public EvaluationResponse evaluate(UUID applicationId, EvaluationRequest request) {
		AppUser evaluator = authenticatedUserService.currentUser();
		JobApplication application = getById(applicationId);

		CandidateEvaluation evaluation = candidateEvaluationRepository.findByApplication(application)
				.map(existing -> {
					existing.update(evaluator, request.score(), request.recommended(), request.comments());
					return existing;
				})
				.orElseGet(() -> candidateEvaluationRepository.save(new CandidateEvaluation(
						application,
						evaluator,
						request.score(),
						request.recommended(),
						request.comments())));

		notificationService.notify(application.getCandidate(), "Nova avaliação registrada",
				"Sua candidatura para " + application.getJobOpening().getTitle() + " recebeu uma avaliação.");

		return EvaluationResponse.from(evaluation);
	}

	@Transactional(readOnly = true)
	public JobApplication getById(UUID applicationId) {
		return jobApplicationRepository.findById(applicationId)
				.orElseThrow(() -> new ResourceNotFoundException("Job application not found."));
	}
}
