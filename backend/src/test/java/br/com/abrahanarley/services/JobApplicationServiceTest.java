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
import br.com.abrahanarley.enums.ApplicationStatus;
import br.com.abrahanarley.exceptions.BusinessException;
import br.com.abrahanarley.repositories.CandidateEvaluationRepository;
import br.com.abrahanarley.repositories.JobApplicationRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
		when(jobOpeningService.getById(TestFixtures.JOB_ID)).thenReturn(job);
		when(jobApplicationRepository.existsByCandidateAndJobOpening(candidate, job)).thenReturn(false);
		when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(savedApplication);

		JobApplicationResponse response = jobApplicationService.apply(TestFixtures.JOB_ID,
				new JobApplicationRequest("Quero crescer na empresa."));

		ArgumentCaptor<JobApplication> application = ArgumentCaptor.forClass(JobApplication.class);
		verify(jobApplicationRepository).save(application.capture());
		assertThat(application.getValue().getCandidate()).isEqualTo(candidate);
		assertThat(application.getValue().getJobOpening()).isEqualTo(job);
		assertThat(application.getValue().getMotivation()).isEqualTo("Quero crescer na empresa.");
		assertThat(response.id()).isEqualTo(TestFixtures.APPLICATION_ID);
		assertThat(response.jobId()).isEqualTo(TestFixtures.JOB_ID);
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
		when(jobOpeningService.getById(TestFixtures.CLOSED_JOB_ID)).thenReturn(job);

		assertThatThrownBy(() -> jobApplicationService.apply(TestFixtures.CLOSED_JOB_ID,
				new JobApplicationRequest("Tenho interesse.")))
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
		when(jobOpeningService.getById(TestFixtures.JOB_ID)).thenReturn(job);
		when(jobApplicationRepository.existsByCandidateAndJobOpening(candidate, job)).thenReturn(true);

		assertThatThrownBy(() -> jobApplicationService.apply(TestFixtures.JOB_ID,
				new JobApplicationRequest("Tenho interesse.")))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Candidate already applied to this job opening.");

		verify(jobApplicationRepository, never()).save(any());
		verify(notificationService, never()).notify(any(), any(), any());
	}

	@Test
	void listMineShouldReturnCandidateApplicationsPaged() {
		AppUser admin = TestFixtures.admin();
		AppUser candidate = TestFixtures.candidate();
		JobOpening job = TestFixtures.openJob(admin);
		JobApplication application = TestFixtures.application(candidate, job);
		PageRequest pageable = PageRequest.of(0, 10);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(jobApplicationRepository.findByCandidate(candidate, pageable))
				.thenReturn(new PageImpl<>(List.of(application), pageable, 1));

		Page<JobApplicationResponse> response = jobApplicationService.listMine(pageable);

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent()).hasSize(1);
		assertThat(response.getContent().getFirst().id()).isEqualTo(TestFixtures.APPLICATION_ID);
	}

	@Test
	void updateStatusShouldNotifyCandidate() {
		AppUser admin = TestFixtures.admin();
		AppUser candidate = TestFixtures.candidate();
		JobOpening job = TestFixtures.openJob(admin);
		JobApplication application = TestFixtures.application(candidate, job);
		when(jobApplicationRepository.findById(TestFixtures.APPLICATION_ID)).thenReturn(Optional.of(application));

		JobApplicationResponse response = jobApplicationService.updateStatus(TestFixtures.APPLICATION_ID,
				new ApplicationStatusRequest(ApplicationStatus.APPROVED, "Parabens, candidatura aprovada."));

		assertThat(response.status()).isEqualTo(ApplicationStatus.APPROVED);
		assertThat(response.feedback()).isEqualTo("Parabens, candidatura aprovada.");
		verify(notificationService).notify(candidate, "Status da candidatura atualizado",
				"Sua candidatura para Analista Java agora estÃ¡ como APPROVED.");
	}

	@Test
	void evaluateShouldUpdateExistingEvaluationAndNotifyCandidate() {
		AppUser admin = TestFixtures.admin();
		AppUser candidate = TestFixtures.candidate();
		JobOpening job = TestFixtures.openJob(admin);
		JobApplication application = TestFixtures.application(candidate, job);
		CandidateEvaluation evaluation = TestFixtures.evaluation(application, admin);
		when(authenticatedUserService.currentUser()).thenReturn(admin);
		when(jobApplicationRepository.findById(TestFixtures.APPLICATION_ID)).thenReturn(Optional.of(application));
		when(candidateEvaluationRepository.findByApplication(application)).thenReturn(Optional.of(evaluation));

		EvaluationResponse response = jobApplicationService.evaluate(TestFixtures.APPLICATION_ID,
				new EvaluationRequest(5, true, "Excelente aderência."));

		assertThat(response.score()).isEqualTo(5);
		assertThat(response.recommended()).isTrue();
		assertThat(response.comments()).isEqualTo("Excelente aderência.");
		verify(candidateEvaluationRepository, never()).save(any());
		verify(notificationService).notify(candidate, "Nova avaliação registrada",
				"Sua candidatura para Analista Java recebeu uma avaliação.");
	}
}
