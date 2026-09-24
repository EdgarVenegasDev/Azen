package com.azue.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class AuthServiceApplicationTests {

	@Test
	void applicationClassShouldBeAnnotatedAsSpringBootApplication() {
		org.junit.jupiter.api.Assertions.assertTrue(
				AuthServiceApplication.class.isAnnotationPresent(SpringBootApplication.class)
		);
	}

}
