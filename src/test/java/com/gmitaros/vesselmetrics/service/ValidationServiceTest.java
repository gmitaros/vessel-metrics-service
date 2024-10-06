package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest
class ValidationServiceTest {

    private ValidationService validationService;
    private VesselData vesselData;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
        vesselData = VesselData.builder()
                .vesselCode("3001")
                .dateTime(null)
                .latitude(37.7749)
                .longitude(122.4194)
                .actualSpeedOverground(12.5)
                .proposedSpeedOverground(10.0)
                .validationErrors(new ArrayList<>())
                .build();
    }

    @Test
    void testValidate_ValidData() {
        vesselData.setDateTime(Utils.parseDateTime("2023-01-01 12:00:00"));
        validationService.validate(vesselData);

        assertEquals(ValidationStatus.VALID, vesselData.getValidationStatus());
        assertTrue(vesselData.getValidationErrors().isEmpty(), "There should be no validation errors for valid data.");
    }

    @Test
    void testValidate_MissingVesselCode() {
        vesselData.setVesselCode(null); // Missing vessel code
        validationService.validate(vesselData);

        assertEquals(ValidationStatus.INVALID, vesselData.getValidationStatus());
        assertTrue(vesselData.getValidationErrors().stream()
                .anyMatch(error -> error.getProblemType() == ValidationProblemType.MISSING_VESSEL_CODE), "Missing vessel code error should be present.");
    }

    @Test
    void testValidate_MissingDateTime() {
        validationService.validate(vesselData);

        assertEquals(ValidationStatus.INVALID, vesselData.getValidationStatus());
        assertTrue(vesselData.getValidationErrors().stream()
                .anyMatch(error -> error.getProblemType() == ValidationProblemType.MISSING_DATETIME), "Missing date-time error should be present.");
    }

    @Test
    void testValidate_NegativeSpeed() {
        vesselData.setActualSpeedOverground(-5.0); // Negative actual speed
        validationService.validate(vesselData);

        assertEquals(ValidationStatus.INVALID, vesselData.getValidationStatus());
        assertTrue(vesselData.getValidationErrors().stream()
                .anyMatch(error -> error.getProblemType() == ValidationProblemType.NEGATIVE_ACTUAL_SPEED), "Negative actual speed error should be present.");
    }

    @Test
    void testValidate_NegativeProposedSpeed() {
        vesselData.setProposedSpeedOverground(-3.0); // Negative proposed speed
        validationService.validate(vesselData);

        assertEquals(ValidationStatus.INVALID, vesselData.getValidationStatus());
        assertTrue(vesselData.getValidationErrors().stream()
                .anyMatch(error -> error.getProblemType() == ValidationProblemType.NEGATIVE_PROPOSED_SPEED), "Negative proposed speed error should be present.");
    }

    @Test
    void testValidate_MissingLongitude() {
        vesselData.setLongitude(null); // Missing longitude
        validationService.validate(vesselData);

        assertEquals(ValidationStatus.INVALID, vesselData.getValidationStatus());
        assertTrue(vesselData.getValidationErrors().stream()
                .anyMatch(error -> error.getProblemType() == ValidationProblemType.MISSING_LONGITUDE), "Missing longitude error should be present.");
    }

    @Test
    void testValidate_MissingLatitude() {
        vesselData.setLatitude(null); // Missing latitude
        validationService.validate(vesselData);

        assertEquals(ValidationStatus.INVALID, vesselData.getValidationStatus());
        assertTrue(vesselData.getValidationErrors().stream()
                .anyMatch(error -> error.getProblemType() == ValidationProblemType.MISSING_LATITUDE), "Missing latitude error should be present.");
    }
}
