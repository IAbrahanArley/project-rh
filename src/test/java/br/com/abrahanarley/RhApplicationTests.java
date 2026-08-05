package br.com.abrahanarley;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RhApplicationTests {

	@Test
	void applicationClassLoads() {
		assertThat(RhApplication.class).isNotNull();
	}

}
