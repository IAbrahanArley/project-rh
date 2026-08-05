package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.EvaluationRequest;
import br.com.abrahanarley.dto.request.JobApplicationRequest;
import br.com.abrahanarley.dto.response.EvaluationResponse;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.CandidateEvaluation;
import br.com.abrahanarley.entities.JobApplication;
import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.exceptions.BusinessException;
import br.com.abrahanarley.repositories.CandidateEvaluationRepository;
import br.com.abrahanarley.repositories.JobApplicationRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

	@Mock
	private JobApplicationRepository jobApplicationRepository;

	@Mock
	private CandidateEvaluationRepository candidateEvaluationRepository;

	@Mock
	private JobOpeningService jobOpeningService;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private JobApplicationService jobApplicationService;

	@Test
	void applyShouldCreateApplicationAndNotifyCandidateAndRecruiter() {
		AppUser admin = TestFixtures.admin();
		AppUser candidate = TestFixtures.candidate();
		JobOpening job = TestFixtures.openJob(admin);
		JobApplication savedApplication = TestFixtures.application(candidate, job);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(jobOpeningService.getById(10L)).thenReturn(job);
		when(jobApplicationRepository.existsByCandidateAndJobOpening(candidate, job)).thenReturn(false);
		when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(savedApplication);

		JobApplicationResponse response = jobApplicationService.apply(10L,
				new JobApplicationRequest("Quero crescer na empresa."));

		ArgumentCaptor<JobApplication> application = ArgumentCaptor.forClass(JobApplication.class);
		verify(jobApplicationRepository).save(application.capture());
		assertThat(application.getValue().getCandidate()).isEqualTo(candidate);
		assertThat(application.getValue().getJobOpening()).isEqualTo(job);
		assertThat(application.getValue().getMotivation()).isEqualTo("Quero crescer na empresa.");
		assertThat(response.id()).isEqualTo(20L);
		assertThat(response.jobId()).isEqualTo(10L);
		verify(notificationService).notify(candidate, "Candidatura recebida",
				"Sua candidatura para Analista Java foi registrada.");
		verify(notificationService).notify(admin, "Nova candidatura",
				"Colaborador Teste candidatou-se para Analista Java.");
	}

	@Test
	void applyShouldRejectClosedJobOpening() {
		AppUser admin = TestFixtures.admin();
		AppUser candidate = TestFixtures.candidate();
		JobOpening job = TestFixtures.closedJob(admin);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(jobOpeningService.getById(11L)).thenReturn(job);

		assertThatThrownBy(() -> jobApplicationService.apply(11L, new JobApplicationRequest("Tenho interesse.")))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Only open job positions can receive applications.");

		verify(jobApplicationRepository, never()).save(any());
		verify(notificationService, never()).notify(any(), any(), any());
	}

	@Test
	void applyShouldRejectDuplicateCandidateApplication() {
		AppUser admin = TestFixtures.admin();
		AppUser candidate = TestFixtures.candidate();
		JobOpening job = TestFixtures.openJob(admin);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(jobOpeningService.getById(10L)).thenReturn(job);
		when(jobApplicationRepository.existsByCandidateAndJobOpening(candidate, job)).thenReturn(true);

		assertThatThrownBy(() -> jobApplicationService.apply(10L, new JobApplicationRequest("Tenho interesse.")))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Candidate already applied to this job opening.");

		verify(jobApplicationRepository, never()).save(any());
		verify(notificationService, never()).notify(any(), any(), any());
	}

	@Test
	void evaluateShouldUpdateExistingEvaluationAndNotifyCandidate() {
		AppUser admin = TestFixtures.admin();
		AppUser candidate = TestFixtures.candidate();
		JobOpening job = TestFixtures.openJob(admin);
		JobApplication application = TestFixtures.application(candidate, job);
		CandidateEvaluation evaluation = TestFixtures.evaluation(application, admin);
		when(authenticatedUserService.currentUser()).thenReturn(admin);
		when(jobApplicationRepository.findById(20L)).thenReturn(Optional.of(application));
		when(candidateEvaluationRepository.findByApplication(application)).thenReturn(Optional.of(evaluation));

		EvaluationResponse response = jobApplicationService.evaluate(20L,
				new EvaluationRequest(5, true, "Excelente aderencia."));

		assertThat(response.score()).isEqualTo(5);
		assertThat(response.recommended()).isTrue();
		assertThat(response.comments()).isEqualTo("Excelente aderencia.");
		verify(candidateEvaluationRepository, never()).save(any());
		verify(notificationService).notify(candidate, "Nova avaliacao registrada",
				"Sua candidatura para Analista Java recebeu uma avaliacao.");
	}
}
