package com.gmitaros.vesselmetrics.parser.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class CsvParserServiceIntegrationTest {

    @Autowired
    private CsvParserService csvParserService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testCsvParsingAndDatabaseInsert() {
        // Prepare a mock CSV file input stream
        InputStream csvInputStream = getClass().getResourceAsStream("/data/vessel_data_test.csv");

        // Invoke the CSV parser service
        csvParserService.parseAndSave(csvInputStream);

        // Check if the data was inserted into the PostgreSQL DB
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vessel_data", Integer.class);
        assertThat(count).isGreaterThan(0);

        // Check for some inserted records (e.g., check validation or outliers)
        Integer validationErrorCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vessel_data_validation_errors", Integer.class);
        assertThat(validationErrorCount).isGreaterThan(0);
    }
}
