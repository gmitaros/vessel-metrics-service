package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.VesselData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for calculating metrics such as speed difference and fuel efficiency
 * for vessel data.
 */
@Service
public class MetricsCalculationService {
    private static final Logger log = LoggerFactory.getLogger(MetricsCalculationService.class);

    /**
     * Calculates the speed difference and fuel efficiency metrics for the given vessel data.
     * Logs and handles any potential issues with the data values.
     *
     * @param data The vessel data object to calculate metrics for.
     */
    public void calculateMetrics(VesselData data) {
        if (data == null) {
            log.error("Vessel data is null, cannot calculate metrics.");
            return;
        }
        calculateSpeedDifference(data);
        calculateFuelEfficiency(data);
    }

    /**
     * Calculates the speed difference between actual speed and proposed speed for the vessel data.
     *
     * @param data The vessel data object.
     */
    private void calculateSpeedDifference(VesselData data) {
        Double actualSpeed = data.getActualSpeedOverground();
        Double proposedSpeed = data.getProposedSpeedOverground();
        if (actualSpeed != null && proposedSpeed != null) {
            data.setSpeedDifference(actualSpeed - proposedSpeed);

        }
    }

    /**
     * Calculates the fuel efficiency (fuel consumption divided by actual speed) for the vessel data.
     *
     * @param data The vessel data object.
     */
    private void calculateFuelEfficiency(VesselData data) {
        Double actualSpeed = data.getActualSpeedOverground();
        Double fuelConsumption = data.getFuelConsumption();

        if (actualSpeed != null && actualSpeed != 0 && fuelConsumption != null) {
            data.setFuelEfficiency(fuelConsumption / actualSpeed);
        }
    }

}
