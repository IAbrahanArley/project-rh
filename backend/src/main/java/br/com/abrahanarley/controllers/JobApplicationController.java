package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.ApplicationStatusRequest;
import br.com.abrahanarley.dto.request.EvaluationRequest;
import br.com.abrahanarley.dto.response.EvaluationResponse;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.dto.response.PageResponse;
import br.com.abrahanarley.services.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController implements JobApplicationApi {

	private final JobApplicationService jobApplicationService;

	public JobApplicationController(JobApplicationService jobApplicationService) {
		this.jobApplicationService = jobApplicationService;
	}

	@Override
	@PreAuthorize("hasRole('CANDIDATE')")
	public PageResponse<JobApplicationResponse> listMine(
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable) {
		return PageResponse.from(jobApplicationService.listMine(pageable));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public JobApplicationResponse updateStatus(@PathVariable UUID id,
			@RequestBody @Valid ApplicationStatusRequest request) {
		return jobApplicationService.updateStatus(id, request);
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public EvaluationResponse evaluate(@PathVariable UUID id, @RequestBody @Valid EvaluationRequest request) {
		return jobApplicationService.evaluate(id, request);
	}
}
