package com.gmitaros.vesselmetrics.util;

import com.gmitaros.vesselmetrics.dto.VesselDataDTO;
import com.gmitaros.vesselmetrics.model.VesselData;
import lombok.experimental.UtilityClass;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.UUID;

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

    /**
     * Safely parses a String into a Double. If the input is not a valid number, returns null.
     *
     * @param value the input String to parse
     * @return the parsed Double or null if the input is invalid
     */
    public Double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Maps a CSV record to a {@link VesselData} object.
     *
     * @param record the CSV record to map
     * @return the mapped {@link VesselData} object, or null if mapping fails
     */
    public VesselData mapCsvRecordToVesselData(CSVRecord record) {
        try {
            final String vesselCode = record.get("vessel_code");
            final String dateTime = record.get("datetime");
            final Double latitude = parseDoubleSafe(record.get("latitude"));
            final Double longitude = parseDoubleSafe(record.get("longitude"));
            final Double power = parseDoubleSafe(record.get("power"));
            final Double fuelConsumption = parseDoubleSafe(record.get("fuel_consumption"));
            final Double actualSpeedOverground = parseDoubleSafe(record.get("actual_speed_overground"));
            final Double proposedSpeedOverground = parseDoubleSafe(record.get("proposed_speed_overground"));
            final Double predictedFuelConsumption = parseDoubleSafe(record.get("predicted_fuel_consumption"));

            return VesselData.builder()
                    .vesselDataUuid(UUID.randomUUID().toString())
                    .vesselCode(vesselCode)
                    .dateTime(Utils.parseDateTime(dateTime))
                    .latitude(latitude)
                    .longitude(longitude)
                    .power(power)
                    .fuelConsumption(fuelConsumption)
                    .actualSpeedOverground(actualSpeedOverground)
                    .proposedSpeedOverground(proposedSpeedOverground)
                    .predictedFuelConsumption(predictedFuelConsumption)
                    .validationErrors(new ArrayList<>())
                    .build();

        } catch (Exception e) {
            log.error("Error mapping CSV record: {}", e.getMessage());
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
