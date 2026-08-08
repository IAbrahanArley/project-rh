package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.CompleteResumeUploadRequest;
import br.com.abrahanarley.dto.request.ResumeUploadUrlRequest;
import br.com.abrahanarley.dto.response.ResumeDownloadUrlResponse;
import br.com.abrahanarley.dto.response.ResumeResponse;
import br.com.abrahanarley.dto.response.ResumeUploadUrlResponse;
import br.com.abrahanarley.services.CandidateResumeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/candidates")
public class CandidateResumeController implements CandidateResumeApi {

	private final CandidateResumeService candidateResumeService;

	public CandidateResumeController(CandidateResumeService candidateResumeService) {
		this.candidateResumeService = candidateResumeService;
	}

	@Override
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResponseEntity<ResumeResponse> findMine() {
		return candidateResumeService.findMine()
				.<ResponseEntity<ResumeResponse>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@Override
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResumeUploadUrlResponse createUploadUrl(@RequestBody @Valid ResumeUploadUrlRequest request) {
		return candidateResumeService.createUploadUrl(request);
	}

	@Override
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResumeResponse completeUpload(@RequestBody @Valid CompleteResumeUploadRequest request) {
		return candidateResumeService.completeUpload(request);
	}

	@Override
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResumeDownloadUrlResponse createMineDownloadUrl() {
		return candidateResumeService.createMineDownloadUrl();
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ResumeDownloadUrlResponse createCandidateDownloadUrl(@PathVariable UUID candidateId) {
		return candidateResumeService.createCandidateDownloadUrl(candidateId);
	}
}
