package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.VesselData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest
class MetricsCalculationServiceTest {

    private MetricsCalculationService metricsCalculationService;

    @BeforeEach
    void setUp() {
        metricsCalculationService = new MetricsCalculationService();
    }

    @Test
    void testCalculateMetrics_ValidData() {
        VesselData vesselData = VesselData.builder()
                .actualSpeedOverground(12.0)
                .proposedSpeedOverground(10.0)
                .fuelConsumption(100.0)
                .build();

        metricsCalculationService.calculateMetrics(vesselData);

        assertEquals(2.0, vesselData.getSpeedDifference(), "Speed difference calculation is incorrect.");
        assertEquals(8.33, vesselData.getFuelEfficiency(), 0.01, "Fuel efficiency calculation is incorrect.");
    }

    @Test
    void testCalculateMetrics_NullSpeed() {
        VesselData vesselData = VesselData.builder()
                .proposedSpeedOverground(10.0)
                .fuelConsumption(100.0)
                .build();

        metricsCalculationService.calculateMetrics(vesselData);

        assertNull(vesselData.getSpeedDifference(), "Speed difference should be null.");
        assertNull(vesselData.getFuelEfficiency(), "Fuel efficiency should be null.");
    }

    @Test
    void testCalculateMetrics_ZeroActualSpeed() {
        VesselData vesselData = VesselData.builder()
                .actualSpeedOverground(0.0)
                .fuelConsumption(100.0)
                .build();

        metricsCalculationService.calculateMetrics(vesselData);

        assertNull(vesselData.getFuelEfficiency(), "Fuel efficiency should be null when actual speed is zero.");
    }

    @Test
    void testCalculateMetrics_NullData() {
        VesselData vesselData = null;
        metricsCalculationService.calculateMetrics(vesselData);
        // This test passes if no exception is thrown
    }
}
