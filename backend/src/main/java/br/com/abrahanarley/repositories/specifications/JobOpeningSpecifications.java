package br.com.abrahanarley.repositories.specifications;

import br.com.abrahanarley.dto.request.JobOpeningFilter;
import br.com.abrahanarley.entities.JobOpening;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class JobOpeningSpecifications {

	private JobOpeningSpecifications() {
	}

	public static Specification<JobOpening> matching(JobOpeningFilter filter) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filter.status() != null) {
				predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
			}

			if (hasText(filter.department())) {
				predicates.add(criteriaBuilder.like(
						criteriaBuilder.lower(root.get("department")),
						contains(filter.department())));
			}

			if (hasText(filter.location())) {
				predicates.add(criteriaBuilder.like(
						criteriaBuilder.lower(root.get("location")),
						contains(filter.location())));
			}

			if (hasText(filter.query())) {
				String queryText = contains(filter.query());
				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), queryText),
						criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), queryText),
						criteriaBuilder.like(criteriaBuilder.lower(root.get("requirements")), queryText)));
			}

			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private static String contains(String value) {
		return "%" + value.trim().toLowerCase() + "%";
	}
}
