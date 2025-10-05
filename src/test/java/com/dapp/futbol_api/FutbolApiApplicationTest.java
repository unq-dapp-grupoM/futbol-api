package com.dapp.futbol_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;
import org.springframework.test.context.TestPropertySource;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@TestPropertySource(properties = {
    "api.security.key=test-api-key",
    "api.security.jwt.secret-key=bXlzdXBlcnNlY3JldGtleWZvcnRlc3RpbmdwdXJwb3Nlc2FuZGl0c2hvdWxkYmVsb25nZW5vdWdo",
    "api.security.jwt.expiration-ms=3600000"
})
class FutbolApiApplicationTest {

	@Test
	void contextLoads() {
		// This test will pass if the application context can be loaded successfully.
	}

	@Test
	void testMainMethod() {
		// Use Mockito to mock the static SpringApplication.run method
		try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
			// Act: Call the main method
			FutbolApiApplication.main(new String[]{});
			// Assert: Verify that SpringApplication.run was called exactly once
			mocked.verify(() -> SpringApplication.run(FutbolApiApplication.class, new String[]{}));
		}
	}
}