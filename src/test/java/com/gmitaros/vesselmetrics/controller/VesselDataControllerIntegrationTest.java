package com.gmitaros.vesselmetrics.controller;

import com.gmitaros.vesselmetrics.dto.ComplianceComparisonResponseDTO;
import com.gmitaros.vesselmetrics.dto.PaginatedResponse;
import com.gmitaros.vesselmetrics.dto.ProblematicWaypointGroupDTO;
import com.gmitaros.vesselmetrics.dto.SpeedDifferenceDTO;
import com.gmitaros.vesselmetrics.dto.ValidationIssueDTO;
import com.gmitaros.vesselmetrics.dto.VesselDataDTO;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class VesselDataControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetSpeedDifferences() {
        String vesselCode = "19310";

        ResponseEntity<PaginatedResponse<SpeedDifferenceDTO>> response = restTemplate.exchange(
                "/vessels/{vesselCode}/speed-differences?page=0&size=10",
                GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                vesselCode
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotEmpty();
        assertThat(response.getBody().getContent().size()).isEqualTo(10);
        assertThat(response.getBody().getTotalElements()).isEqualTo(34);
    }

    @Test
    void testGetValidationIssues() {
        String vesselCode = "19310";

        ResponseEntity<List<ValidationIssueDTO>> response = restTemplate.exchange(
                "/vessels/{vesselCode}/validation-issues",
                GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                vesselCode
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().size()).isEqualTo(6);
    }

    @Test
    void testCompareVesselCompliance() {
        String vesselCode1 = "19310";
        String vesselCode2 = "3001";

        ResponseEntity<ComplianceComparisonResponseDTO> response = restTemplate.getForEntity(
                "/vessels/compare-compliance?vesselCode1={vesselCode1}&vesselCode2={vesselCode2}",
                ComplianceComparisonResponseDTO.class,
                vesselCode1, vesselCode2
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResult()).isNotBlank();
        assertThat(response.getBody().getResult()).isEqualTo("19310 is more compliant.");
        assertThat(response.getBody().getCompliance1()).isCloseTo(2.148288849167802, Percentage.withPercentage(5));
    }

    @Test
    void testGetDataMerge() {
        String vesselCode = "3001";
        String startDate = "2023-06-01T00:00:00";
        String endDate = "2023-06-10T23:59:59";

        ResponseEntity<PaginatedResponse<VesselDataDTO>> response = restTemplate.exchange(
                "/vessels/{vesselCode}/data-merge?startDate={startDate}&endDate={endDate}&page=0&size=10",
                GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                vesselCode, startDate, endDate
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(868);
    }

    @Test
    void testGetProblematicWaypoints() {
        String vesselCode = "3001";

        ResponseEntity<List<ProblematicWaypointGroupDTO>> response = restTemplate.exchange(
                "/vessels/{vesselCode}/problematic-waypoints",
                GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                vesselCode
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().size()).isEqualTo(198);
    }
}