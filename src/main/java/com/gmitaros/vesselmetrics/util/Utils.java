package com.gmitaros.vesselmetrics.util;

import com.gmitaros.vesselmetrics.dto.VesselDataDTO;
import com.gmitaros.vesselmetrics.model.VesselData;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public LocalDateTime parseDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date-time format: {}", dateTimeStr);
            return null;
        }
    }

    public VesselDataDTO mapToVesselDataDTO(VesselData vesselData) {
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
