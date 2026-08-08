package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.JobOpeningFilter;
import br.com.abrahanarley.dto.response.JobOpeningResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.enums.JobStatus;
import br.com.abrahanarley.repositories.JobOpeningRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobOpeningServiceTest {

	@Mock
	private JobOpeningRepository jobOpeningRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private JobOpeningService jobOpeningService;

	@Test
	void listShouldSearchUsingSpecificationAndPageable() {
		AppUser admin = TestFixtures.admin();
		JobOpening job = TestFixtures.openJob(admin);
		PageRequest pageable = PageRequest.of(0, 10);
		JobOpeningFilter filter = new JobOpeningFilter(JobStatus.OPEN, "java", "Tecnologia", "Remoto");
		when(jobOpeningRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(job), pageable, 1));

		Page<JobOpeningResponse> response = jobOpeningService.list(filter, pageable);

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().getFirst().title()).isEqualTo("Analista Java");
		verify(jobOpeningRepository).findAll(any(Specification.class), eq(pageable));
	}
}
