package com.github.rodriguesla.api_simulador;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiSimuladorApplicationTest {

	@Test
	void contextLoads() {
	}

	@Test
	void mainMethodRuns() {
		System.setProperty("spring.main.web-application-type", "none");
		try {
			ApiSimuladorApplication.main(new String[]{"--spring.main.web-application-type=none"});
		} finally {
			System.clearProperty("spring.main.web-application-type");
		}
	}

}
