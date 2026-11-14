package com.paulaoramas.tetris.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresTestcontainersBaselineTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void postgresql_container_is_running_and_healthy() {
        assertThat(postgres.isRunning()).isTrue();
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void flyway_migrations_applied_schema_tables_successfully() {
        List<String> tables = jdbcTemplate.query(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                (rs, rowNum) -> rs.getString("table_name")
        );

        assertThat(tables)
                .contains(
                        "flyway_schema_history",
                        "users",
                        "games",
                        "game_moves"
                );
    }
}
