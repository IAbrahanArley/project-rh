package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.CompleteResumeUploadRequest;
import br.com.abrahanarley.dto.request.ResumeUploadUrlRequest;
import br.com.abrahanarley.dto.response.ResumeResponse;
import br.com.abrahanarley.dto.response.ResumeUploadUrlResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.CandidateResume;
import br.com.abrahanarley.exceptions.BusinessException;
import br.com.abrahanarley.repositories.CandidateResumeRepository;
import br.com.abrahanarley.repositories.UserRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateResumeServiceTest {

	@Mock
	private CandidateResumeRepository candidateResumeRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private ResumeStorageService resumeStorageService;

	@InjectMocks
	private CandidateResumeService candidateResumeService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(candidateResumeService, "keyPrefix", "resumes");
		ReflectionTestUtils.setField(candidateResumeService, "maxSizeBytes", 10_485_760L);
	}

	@Test
	void createUploadUrlShouldReturnCandidateScopedStorageKey() throws MalformedURLException {
		AppUser candidate = TestFixtures.candidate();
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(resumeStorageService.createUploadUrl(any(), any())).thenReturn(new URL("https://bucket/upload"));
		when(resumeStorageService.getUploadExpirationSeconds()).thenReturn(300L);

		ResumeUploadUrlResponse response = candidateResumeService.createUploadUrl(
				new ResumeUploadUrlRequest("currículo.pdf", "application/pdf", 1200));

		assertThat(response.uploadUrl()).isEqualTo("https://bucket/upload");
		assertThat(response.storageKey()).startsWith("resumes/" + TestFixtures.CANDIDATE_ID + "/");
		assertThat(response.storageKey()).endsWith(".pdf");
		assertThat(response.requiredContentType()).isEqualTo("application/pdf");
		assertThat(response.expiresInSeconds()).isEqualTo(300L);
	}

	@Test
	void completeUploadShouldPersistResumeMetadata() {
		AppUser candidate = TestFixtures.candidate();
		String storageKey = "resumes/" + TestFixtures.CANDIDATE_ID + "/resume.pdf";
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(resumeStorageService.headObject(storageKey)).thenReturn(HeadObjectResponse.builder()
				.contentLength(1200L)
				.contentType("application/pdf")
				.build());
		when(candidateResumeRepository.findByCandidate(candidate)).thenReturn(Optional.empty());
		when(candidateResumeRepository.save(any(CandidateResume.class))).thenAnswer(invocation -> {
			CandidateResume resume = invocation.getArgument(0);
			ReflectionTestUtils.setField(resume, "id", TestFixtures.OTHER_USER_ID);
			ReflectionTestUtils.setField(resume, "uploadedAt", OffsetDateTime.parse("2026-08-08T10:00:00Z"));
			return resume;
		});

		ResumeResponse response = candidateResumeService.completeUpload(
				new CompleteResumeUploadRequest(storageKey, "currículo.pdf", "application/pdf", 1200));

		assertThat(response.fileName()).isEqualTo("currículo.pdf");
		assertThat(response.contentType()).isEqualTo("application/pdf");
		assertThat(response.sizeBytes()).isEqualTo(1200);
	}

	@Test
	void completeUploadShouldRejectStorageKeyFromAnotherCandidate() {
		AppUser candidate = TestFixtures.candidate();
		when(authenticatedUserService.currentUser()).thenReturn(candidate);

		assertThatThrownBy(() -> candidateResumeService.completeUpload(new CompleteResumeUploadRequest(
				"resumes/00000000-0000-0000-0000-000000000999/resume.pdf",
				"currículo.pdf",
				"application/pdf",
				1200)))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Storage key is not valid for the authenticated candidate.");
		verify(resumeStorageService, never()).headObject(any());
	}

	@Test
	void completeUploadShouldRejectMismatchedUploadedSize() {
		AppUser candidate = TestFixtures.candidate();
		String storageKey = "resumes/" + TestFixtures.CANDIDATE_ID + "/resume.pdf";
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(resumeStorageService.headObject(storageKey)).thenReturn(HeadObjectResponse.builder()
				.contentLength(100L)
				.contentType("application/pdf")
				.build());

		assertThatThrownBy(() -> candidateResumeService.completeUpload(
				new CompleteResumeUploadRequest(storageKey, "currículo.pdf", "application/pdf", 1200)))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Uploaded resume size does not match the requested size.");
		verify(candidateResumeRepository, never()).save(any());
	}
}
