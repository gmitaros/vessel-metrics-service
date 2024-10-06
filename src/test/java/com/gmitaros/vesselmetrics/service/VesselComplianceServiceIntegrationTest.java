package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.dto.ComplianceComparisonResponseDTO;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest
class VesselComplianceServiceIntegrationTest {

    @Autowired
    private VesselComplianceService vesselComplianceService;

    @Test
    void testCompareVesselCompliance_ValidVessels() {
        String vesselCode1 = "3001";
        String vesselCode2 = "19310";

        ComplianceComparisonResponseDTO response = vesselComplianceService.compareVesselCompliance(vesselCode1, vesselCode2);

        assertNotNull(response);
        assertEquals(vesselCode1, response.getVesselCode1());
        assertEquals(vesselCode2, response.getVesselCode2());
        assertEquals(-217.32789972602322, response.getCompliance1(), 0.01, "Compliance 1 is incorrect");
        assertEquals(2.148288849167802, response.getCompliance2(), 0.01, "Compliance 2 is incorrect");
        assertEquals(response.getResult(), "19310 is more compliant.");
    }

    @Test
    void testCompareVesselCompliance_VesselNotFound() {
        String vesselCode1 = "NON_EXISTENT_1";
        String vesselCode2 = "NON_EXISTENT_2";

        Exception exception = assertThrows(VesselNotFoundException.class, () -> vesselComplianceService.compareVesselCompliance(vesselCode1, vesselCode2));

        String expectedMessage = "Vessel with code " + vesselCode1 + " does not exist.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
