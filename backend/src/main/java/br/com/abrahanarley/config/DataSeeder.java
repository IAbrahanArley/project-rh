package br.com.abrahanarley.config;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.enums.Role;
import br.com.abrahanarley.repositories.JobOpeningRepository;
import br.com.abrahanarley.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

	@Bean
	@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
	CommandLineRunner seedData(UserRepository userRepository, JobOpeningRepository jobOpeningRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByUsername("admin")) {
				userRepository.save(new AppUser(
						"admin",
						passwordEncoder.encode("admin123"),
						"Administrador RH",
						"admin@empresa.com",
						"Recursos Humanos",
						LocalDate.of(2020, 1, 10),
						Role.ADMIN));
			}

			if (!userRepository.existsByUsername("colaborador")) {
				userRepository.save(new AppUser(
						"colaborador",
						passwordEncoder.encode("user123"),
						"Colaborador Interno",
						"colaborador@empresa.com",
						"Tecnologia",
						LocalDate.of(2022, 4, 5),
						Role.CANDIDATE));
			}

			if (jobOpeningRepository.count() == 0) {
				AppUser admin = userRepository.findByUsername("admin").orElseThrow();
				jobOpeningRepository.save(new JobOpening(
						"Desenvolvedor Full Stack Pleno",
						"Atuar no desenvolvimento de soluções internas para recrutamento, carreira e mobilidade.",
						"Experiência com Java, Spring Boot, APIs REST, Angular e banco relacional.",
						"Tecnologia",
						"Recife - PE",
						admin));
			}
		};
	}
}
