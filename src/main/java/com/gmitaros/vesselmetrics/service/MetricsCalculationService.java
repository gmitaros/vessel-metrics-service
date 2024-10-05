package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.VesselData;
import org.springframework.stereotype.Service;

@Service
public class MetricsCalculationService {

    public void calculateMetrics(VesselData data) {
        if (data.getActualSpeedOverground() != null && data.getProposedSpeedOverground() != null) {
            data.setSpeedDifference(data.getActualSpeedOverground() - data.getProposedSpeedOverground());
        }

        if (data.getActualSpeedOverground() != null && data.getActualSpeedOverground() != 0
                && data.getFuelConsumption() != null) {
            data.setFuelEfficiency(data.getFuelConsumption() / data.getActualSpeedOverground());
        }
    }
}
