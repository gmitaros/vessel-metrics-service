package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.ValidationError;
import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.model.VesselMetricsStatistics;
import com.gmitaros.vesselmetrics.repository.ValidationErrorRepository;
import com.gmitaros.vesselmetrics.repository.VesselDataRepository;
import com.gmitaros.vesselmetrics.repository.VesselMetricsStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest
class OutlierDetectionServiceIntegrationTest {

    @Autowired
    private OutlierDetectionService outlierDetectionService;

    @Autowired
    private VesselDataRepository vesselDataRepository;

    @Autowired
    private VesselMetricsStatisticsRepository vesselMetricsStatisticsRepository;

    @Autowired
    private ValidationErrorRepository validationErrorRepository;

    @BeforeEach
    void setUp() {
        // Add setup code here if necessary, like inserting test data into the DB.
    }

    @Test
    void testDetectAndStoreOutliers_ValidData() {
        String vesselCode = "3001";

        // Ensure statistics exist for the given vessel code
        Optional<VesselMetricsStatistics> stats = vesselMetricsStatisticsRepository.findStatisticsByVesselCode(vesselCode);
        assertTrue(stats.isPresent());

        // Trigger the outlier detection process
        outlierDetectionService.findOutlierByVessel(vesselCode);

        // Verify that outliers have been detected and stored
        List<ValidationError> validationErrors = validationErrorRepository.findValidationIssuesByVesselCodeAndProblemType(vesselCode, ValidationProblemType.OUTLIER);
        assertFalse(validationErrors.isEmpty());

        // Verify that some VesselData has been marked as INVALID
        List<VesselData> invalidVesselData = vesselDataRepository.findByVesselCodeAndValidationStatus(vesselCode, ValidationStatus.INVALID);
        assertFalse(invalidVesselData.isEmpty());

        // Verify the integrity of detected outliers and related data
        validationErrors.forEach(error -> {
            assertNotNull(error.getVesselData());
            assertEquals(vesselCode, error.getVesselCode());
            assertEquals(ValidationStatus.INVALID, error.getVesselData().getValidationStatus());
        });
    }

    @Test
    void testDetectAndStoreOutliers_NoData() {
        // Test with a vessel code that has no data
        String vesselCode = "INVALID_VESSEL_CODE";

        // Trigger the outlier detection process for a vessel that does not exist
        outlierDetectionService.findOutlierByVessel(vesselCode);

        // Verify that no outliers were detected or stored
        List<ValidationError> validationErrors = validationErrorRepository.findValidationIssuesByVesselCodeAndProblemType(vesselCode, ValidationProblemType.OUTLIER);
        assertTrue(validationErrors.isEmpty());

        // Verify no VesselData marked as INVALID for the non-existent vessel
        List<VesselData> invalidVesselData = vesselDataRepository.findByVesselCodeAndValidationStatus(vesselCode, ValidationStatus.INVALID);
        assertTrue(invalidVesselData.isEmpty());
    }
}
