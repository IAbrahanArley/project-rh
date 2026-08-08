package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.JobApplicationRequest;
import br.com.abrahanarley.dto.request.JobOpeningRequest;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.dto.response.JobOpeningResponse;
import br.com.abrahanarley.dto.response.PageResponse;
import br.com.abrahanarley.enums.JobStatus;
import br.com.abrahanarley.services.JobApplicationService;
import br.com.abrahanarley.services.JobOpeningService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobOpeningController implements JobOpeningApi {

	private final JobOpeningService jobOpeningService;
	private final JobApplicationService jobApplicationService;

	public JobOpeningController(JobOpeningService jobOpeningService, JobApplicationService jobApplicationService) {
		this.jobOpeningService = jobOpeningService;
		this.jobApplicationService = jobApplicationService;
	}

	@Override
	public PageResponse<JobOpeningResponse> list(
			@RequestParam(required = false) JobStatus status,
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable) {
		return PageResponse.from(jobOpeningService.list(status, pageable));
	}

	@Override
	public JobOpeningResponse findById(@PathVariable UUID id) {
		return jobOpeningService.findById(id);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public JobOpeningResponse create(@RequestBody @Valid JobOpeningRequest request) {
		return jobOpeningService.create(request);
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public JobOpeningResponse update(@PathVariable UUID id, @RequestBody @Valid JobOpeningRequest request) {
		return jobOpeningService.update(id, request);
	}

	@Override
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void cancel(@PathVariable UUID id) {
		jobOpeningService.cancel(id);
	}

	@Override
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('CANDIDATE')")
	public JobApplicationResponse apply(@PathVariable UUID id, @RequestBody @Valid JobApplicationRequest request) {
		return jobApplicationService.apply(id, request);
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public PageResponse<JobApplicationResponse> listApplications(
			@PathVariable UUID id,
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable) {
		return PageResponse.from(jobApplicationService.listByJob(id, pageable));
	}
}
