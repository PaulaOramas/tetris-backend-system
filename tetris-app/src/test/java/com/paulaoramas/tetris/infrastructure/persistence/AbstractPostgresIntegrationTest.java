package com.paulaoramas.tetris.infrastructure.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for database integration tests using a real, ephemeral PostgreSQL instance.
 *
 * <p>Uses the Testcontainers singleton pattern: one container is started once for all integration
 * tests in the suite and shared, avoiding container startup overhead between test classes.
 *
 * <p>Spring Boot dynamically configures datasource connection properties and triggers
 * Flyway migrations against the test database before tests execute.
 */
@SpringBootTest
public abstract class AbstractPostgresIntegrationTest {

    protected static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("tetris_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
