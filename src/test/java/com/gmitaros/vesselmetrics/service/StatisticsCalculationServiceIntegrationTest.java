package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.dto.ProblematicWaypointGroupDTO;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest
class StatisticsCalculationServiceIntegrationTest {

    @Autowired
    private StatisticsCalculationService statisticsCalculationService;

    @BeforeEach
    void setUp() {
        // If necessary, set up initial test data here (or use pre-existing data)
    }

    @Test
    void testGetProblematicWaypointGroups_WithValidVesselCode() {
        String vesselCode = "3001";
        List<ProblematicWaypointGroupDTO> problematicWaypoints = statisticsCalculationService.getProblematicWaypointGroups(vesselCode, null);

        assertNotNull(problematicWaypoints);
        assertFalse(problematicWaypoints.isEmpty(), "There should be problematic waypoints");

        ProblematicWaypointGroupDTO firstGroup = problematicWaypoints.getFirst();
        assertTrue(firstGroup.getProblemCount() > 0, "The problem count should be greater than 0");
        assertNotNull(firstGroup.getWaypoints(), "Waypoints should not be null");
    }

    @Test
    void testGetProblematicWaypointGroups_WithProblemTypeOutlier() {
        String vesselCode = "3001";
        ValidationProblemType problemType = ValidationProblemType.OUTLIER;

        List<ProblematicWaypointGroupDTO> problematicWaypoints = statisticsCalculationService.getProblematicWaypointGroups(vesselCode, problemType);

        assertNotNull(problematicWaypoints);
        assertFalse(problematicWaypoints.isEmpty(), "There should be problematic waypoints for OUTLIER");

        // Further checks on the content of the waypoints
        ProblematicWaypointGroupDTO firstGroup = problematicWaypoints.getFirst();
        assertTrue(firstGroup.getProblemCount() > 0, "The problem count should be greater than 0 for OUTLIER");
    }

    @Test
    void testGetProblematicWaypointGroups_VesselNotFound() {
        String nonExistentVesselCode = "UNKNOWN_VESSEL";

        Exception exception = assertThrows(VesselNotFoundException.class, () -> statisticsCalculationService.getProblematicWaypointGroups(nonExistentVesselCode, null));

        String expectedMessage = "Vessel with code " + nonExistentVesselCode + " does not exist.";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
