package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.dto.ComplianceDTO;
import com.gmitaros.vesselmetrics.dto.SpeedDifferenceDTO;
import com.gmitaros.vesselmetrics.dto.ValidationIssueDTO;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest
class VesselDataServiceIntegrationTest {

    @Autowired
    private VesselDataService vesselDataService;

    @BeforeEach
    void setUp() {
        // Add setup code here if necessary, like inserting test data into the DB.
    }

    @Test
    void testGetSpeedDifferences_ValidVessel() {
        Pageable pageable = PageRequest.of(0, 10);
        String vesselCode = "3001";

        var speedDifferencesPage = vesselDataService.getSpeedDifferences(vesselCode, pageable);

        assertNotNull(speedDifferencesPage);
        assertFalse(speedDifferencesPage.isEmpty());
        SpeedDifferenceDTO speedDifferenceDTO = speedDifferencesPage.getContent().getFirst();
        assertNotNull(speedDifferenceDTO.getDateTime());
        assertNotNull(speedDifferenceDTO.getLatitude());
        assertNotNull(speedDifferenceDTO.getLongitude());
        assertNotNull(speedDifferenceDTO.getSpeedDifference());
    }

    @Test
    void testGetSpeedDifferences_VesselNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        String vesselCode = "VESSEL_CODE";

        Exception exception = assertThrows(VesselNotFoundException.class, () -> vesselDataService.getSpeedDifferences(vesselCode, pageable));

        String expectedMessage = "Vessel with code " + vesselCode + " does not exist.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testGetValidationIssues_ValidVessel() {
        String vesselCode = "3001";

        List<ValidationIssueDTO> validationIssues = vesselDataService.getValidationIssues(vesselCode);

        assertNotNull(validationIssues);
        assertFalse(validationIssues.isEmpty());
        ValidationIssueDTO issue = validationIssues.getFirst();
        assertNotNull(issue.issue());
        assertTrue(issue.frequency() > 0);
    }

    @Test
    void testCalculateCompliance_ValidVessel() {
        String vesselCode = "VALID_VESSEL_CODE";

        ComplianceDTO compliance = vesselDataService.calculateCompliance(vesselCode);

        assertNotNull(compliance);
        assertEquals(vesselCode, compliance.vesselCode());
        assertTrue(compliance.compliancePercentage() >= 0);
    }
}
