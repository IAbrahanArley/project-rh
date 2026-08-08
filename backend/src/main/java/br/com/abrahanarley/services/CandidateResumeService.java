package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.CompleteResumeUploadRequest;
import br.com.abrahanarley.dto.request.ResumeUploadUrlRequest;
import br.com.abrahanarley.dto.response.ResumeDownloadUrlResponse;
import br.com.abrahanarley.dto.response.ResumeResponse;
import br.com.abrahanarley.dto.response.ResumeUploadUrlResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.CandidateResume;
import br.com.abrahanarley.enums.Role;
import br.com.abrahanarley.exceptions.BusinessException;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.CandidateResumeRepository;
import br.com.abrahanarley.repositories.UserRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Service
public class CandidateResumeService {

	private static final String PDF_CONTENT_TYPE = "application/pdf";

	private final CandidateResumeRepository candidateResumeRepository;
	private final UserRepository userRepository;
	private final AuthenticatedUserService authenticatedUserService;
	private final ResumeStorageService resumeStorageService;
	private final String keyPrefix;
	private final long maxSizeBytes;

	public CandidateResumeService(
			CandidateResumeRepository candidateResumeRepository,
			UserRepository userRepository,
			AuthenticatedUserService authenticatedUserService,
			ResumeStorageService resumeStorageService,
			@Value("${app.storage.resumes.key-prefix}") String keyPrefix,
			@Value("${app.storage.resumes.max-size-bytes}") long maxSizeBytes) {
		this.candidateResumeRepository = candidateResumeRepository;
		this.userRepository = userRepository;
		this.authenticatedUserService = authenticatedUserService;
		this.resumeStorageService = resumeStorageService;
		this.keyPrefix = keyPrefix;
		this.maxSizeBytes = maxSizeBytes;
	}

	public Optional<ResumeResponse> findMine() {
		AppUser candidate = authenticatedUserService.currentUser();
		return candidateResumeRepository.findByCandidate(candidate).map(ResumeResponse::from);
	}

	public ResumeUploadUrlResponse createUploadUrl(ResumeUploadUrlRequest request) {
		AppUser candidate = authenticatedUserService.currentUser();
		validatePdf(request.contentType(), request.sizeBytes());
		String storageKey = candidateResumeKey(candidate.getId());
		URL uploadUrl = resumeStorageService.createUploadUrl(storageKey, PDF_CONTENT_TYPE);

		return new ResumeUploadUrlResponse(
				uploadUrl.toString(),
				storageKey,
				resumeStorageService.getUploadExpirationSeconds(),
				PDF_CONTENT_TYPE);
	}

	@Transactional
	public ResumeResponse completeUpload(CompleteResumeUploadRequest request) {
		AppUser candidate = authenticatedUserService.currentUser();
		validatePdf(request.contentType(), request.sizeBytes());
		assertStorageKeyBelongsToCandidate(request.storageKey(), candidate);

		HeadObjectResponse objectMetadata = resumeStorageService.headObject(request.storageKey());
		validateUploadedObject(objectMetadata, request.sizeBytes());

		Optional<CandidateResume> existingResume = candidateResumeRepository.findByCandidate(candidate);
		String previousStorageKey = existingResume.map(CandidateResume::getStorageKey).orElse(null);
		CandidateResume resume = existingResume
				.map(current -> {
					current.update(safeFileName(request.fileName()), PDF_CONTENT_TYPE, request.sizeBytes(),
							request.storageKey());
					return current;
				})
				.orElseGet(() -> new CandidateResume(candidate, safeFileName(request.fileName()), PDF_CONTENT_TYPE,
						request.sizeBytes(), request.storageKey()));

		CandidateResume savedResume = candidateResumeRepository.save(resume);

		if (previousStorageKey != null && !previousStorageKey.equals(request.storageKey())) {
			resumeStorageService.deleteObject(previousStorageKey);
		}

		return ResumeResponse.from(savedResume);
	}

	public ResumeDownloadUrlResponse createMineDownloadUrl() {
		AppUser candidate = authenticatedUserService.currentUser();
		CandidateResume resume = candidateResumeRepository.findByCandidate(candidate)
				.orElseThrow(() -> new ResourceNotFoundException("Resume not found."));
		return createDownloadUrl(resume);
	}

	public ResumeDownloadUrlResponse createCandidateDownloadUrl(UUID candidateId) {
		AppUser candidate = userRepository.findById(candidateId)
				.filter(user -> user.getRole() == Role.CANDIDATE)
				.orElseThrow(() -> new ResourceNotFoundException("Candidate not found."));
		CandidateResume resume = candidateResumeRepository.findByCandidate(candidate)
				.orElseThrow(() -> new ResourceNotFoundException("Resume not found."));
		return createDownloadUrl(resume);
	}

	private ResumeDownloadUrlResponse createDownloadUrl(CandidateResume resume) {
		URL downloadUrl = resumeStorageService.createDownloadUrl(resume.getStorageKey(), resume.getFileName());
		return new ResumeDownloadUrlResponse(downloadUrl.toString(),
				resumeStorageService.getDownloadExpirationSeconds());
	}

	private void validatePdf(String contentType, long sizeBytes) {
		if (!PDF_CONTENT_TYPE.equals(contentType)) {
			throw new BusinessException("Only PDF resumes are supported.");
		}

		if (sizeBytes < 1 || sizeBytes > maxSizeBytes) {
			throw new BusinessException("Resume size exceeds the configured limit.");
		}
	}

	private void validateUploadedObject(HeadObjectResponse objectMetadata, long expectedSizeBytes) {
		if (objectMetadata.contentLength() != expectedSizeBytes) {
			throw new BusinessException("Uploaded resume size does not match the requested size.");
		}

		if (objectMetadata.contentType() != null && !PDF_CONTENT_TYPE.equals(objectMetadata.contentType())) {
			throw new BusinessException("Uploaded resume content type is invalid.");
		}
	}

	private String candidateResumeKey(UUID candidateId) {
		return cleanKeyPrefix() + "/" + candidateId + "/" + UUID.randomUUID() + ".pdf";
	}

	private void assertStorageKeyBelongsToCandidate(String storageKey, AppUser candidate) {
		String candidatePrefix = cleanKeyPrefix() + "/" + candidate.getId() + "/";

		if (!storageKey.startsWith(candidatePrefix) || !storageKey.endsWith(".pdf")) {
			throw new BusinessException("Storage key is not valid for the authenticated candidate.");
		}
	}

	private String cleanKeyPrefix() {
		return keyPrefix.replaceAll("^/+", "").replaceAll("/+$", "");
	}

	private String safeFileName(String fileName) {
		return fileName.trim().replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
	}
}
