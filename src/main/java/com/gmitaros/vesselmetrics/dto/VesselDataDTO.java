package com.gmitaros.vesselmetrics.dto;

import com.gmitaros.vesselmetrics.model.VesselData;

import java.time.LocalDateTime;

public record VesselDataDTO(
        String vesselCode,
        LocalDateTime dateTime,
        Double latitude,
        Double longitude,
        Double power,
        Double fuelConsumption,
        Double actualSpeedOverground,
        Double proposedSpeedOverground,
        Double predictedFuelConsumption,
        Double speedDifference,
        Double fuelEfficiency
) {
    public static VesselDataDTO from(VesselData vesselData) {
        return new VesselDataDTO(
                vesselData.getVesselCode(),
                vesselData.getDateTime(),
                vesselData.getLatitude(),
                vesselData.getLongitude(),
                vesselData.getPower(),
                vesselData.getFuelConsumption(),
                vesselData.getActualSpeedOverground(),
                vesselData.getProposedSpeedOverground(),
                vesselData.getPredictedFuelConsumption(),
                vesselData.getSpeedDifference(),
                vesselData.getFuelEfficiency()
        );
    }


}
