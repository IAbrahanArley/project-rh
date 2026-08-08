package br.com.abrahanarley.support;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.CandidateEvaluation;
import br.com.abrahanarley.entities.JobApplication;
import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.entities.Notification;
import br.com.abrahanarley.enums.JobStatus;
import br.com.abrahanarley.enums.Role;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class TestFixtures {

	public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	public static final UUID CANDIDATE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
	public static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
	public static final UUID JOB_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
	public static final UUID CLOSED_JOB_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
	public static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
	public static final UUID EVALUATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");
	public static final UUID NOTIFICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");
	public static final UUID OTHER_NOTIFICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000041");

	private TestFixtures() {
	}

	public static AppUser admin() {
		return user(ADMIN_ID, "admin", "Administrador RH", Role.ADMIN);
	}

	public static AppUser candidate() {
		return user(CANDIDATE_ID, "colaborador", "Colaborador Teste", Role.CANDIDATE);
	}

	public static AppUser user(UUID id, String username, String fullName, Role role) {
		AppUser user = new AppUser(
				username,
				"{noop}password",
				fullName,
				username + "@empresa.com",
				role == Role.ADMIN ? "Recursos Humanos" : "Tecnologia",
				LocalDate.of(2023, 1, 10),
				role);
		ReflectionTestUtils.setField(user, "id", id);
		ReflectionTestUtils.setField(user, "createdAt", OffsetDateTime.now());
		return user;
	}

	public static JobOpening openJob(AppUser createdBy) {
		return job(JOB_ID, "Analista Java", JobStatus.OPEN, createdBy);
	}

	public static JobOpening closedJob(AppUser createdBy) {
		return job(CLOSED_JOB_ID, "Analista QA", JobStatus.CLOSED, createdBy);
	}

	public static JobOpening job(UUID id, String title, JobStatus status, AppUser createdBy) {
		JobOpening job = new JobOpening(
				title,
				"Descrição da vaga",
				"Conhecimento em Spring Boot",
				"Tecnologia",
				"Remoto",
				createdBy);
		job.update(title, "Descrição da vaga", "Conhecimento em Spring Boot", "Tecnologia", "Remoto", status);
		ReflectionTestUtils.setField(job, "id", id);
		ReflectionTestUtils.setField(job, "createdAt", OffsetDateTime.now().minusDays(1));
		ReflectionTestUtils.setField(job, "updatedAt", OffsetDateTime.now());
		return job;
	}

	public static JobApplication application(AppUser candidate, JobOpening jobOpening) {
		JobApplication application = new JobApplication(candidate, jobOpening, "Quero crescer na empresa.");
		ReflectionTestUtils.setField(application, "id", APPLICATION_ID);
		ReflectionTestUtils.setField(application, "createdAt", OffsetDateTime.now().minusHours(2));
		ReflectionTestUtils.setField(application, "updatedAt", OffsetDateTime.now().minusHours(1));
		return application;
	}

	public static CandidateEvaluation evaluation(JobApplication application, AppUser evaluator) {
		CandidateEvaluation evaluation = new CandidateEvaluation(application, evaluator, 3, false, "Em analise.");
		ReflectionTestUtils.setField(evaluation, "id", EVALUATION_ID);
		ReflectionTestUtils.setField(evaluation, "createdAt", OffsetDateTime.now().minusHours(1));
		ReflectionTestUtils.setField(evaluation, "updatedAt", OffsetDateTime.now().minusMinutes(30));
		return evaluation;
	}

	public static Notification notification(UUID id, AppUser recipient) {
		Notification notification = new Notification(recipient, "Assunto", "Mensagem");
		ReflectionTestUtils.setField(notification, "id", id);
		ReflectionTestUtils.setField(notification, "createdAt", OffsetDateTime.now());
		return notification;
	}
}
