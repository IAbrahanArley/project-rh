package br.com.abrahanarley.entities;

import br.com.abrahanarley.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 80)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 120)
	private String fullName;

	@Column(nullable = false, unique = true, length = 160)
	private String email;

	@Column(nullable = false, length = 80)
	private String department;

	@Column(nullable = false)
	private LocalDate hireDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Role role;

	@Column(nullable = false)
	private boolean active = true;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected AppUser() {
	}

	public AppUser(String username, String password, String fullName, String email, String department,
			LocalDate hireDate, Role role) {
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.email = email;
		this.department = department;
		this.hireDate = hireDate;
		this.role = role;
	}

	@PrePersist
	void prePersist() {
		createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getDepartment() {
		return department;
	}

	public LocalDate getHireDate() {
		return hireDate;
	}

	public Role getRole() {
		return role;
	}

	public boolean isActive() {
		return active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
