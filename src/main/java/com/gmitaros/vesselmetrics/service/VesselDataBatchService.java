package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.VesselData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for batch-saving vessel data and associated validation errors into the database.
 */
@RequiredArgsConstructor
@Service
public class VesselDataBatchService {

    private static final Logger log = LoggerFactory.getLogger(VesselDataBatchService.class);
    private final JdbcTemplate jdbcTemplate;

    // SQL Insert Statement for VesselData Batch Insert
    private static final String SQL_INSERT_VESSEL_DATA = """
            INSERT INTO vessel_data (vessel_data_uuid, vessel_code, date_time, latitude, longitude, power, fuel_consumption, actual_speed_overground, proposed_speed_overground, predicted_fuel_consumption, speed_difference, fuel_efficiency, validation_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    // SQL Insert Statement for Validation Errors Batch Insert
    private static final String SQL_INSERT_VALIDATION_ERRORS = """
            INSERT INTO vessel_data_validation_errors (vessel_data_uuid, vessel_code, error_message, problem_type)
            VALUES (?, ?, ?, ?)
            """;

    /**
     * Saves a batch of vessel data into the database.
     *
     * @param vesselDataBatch the list of vessel data to save
     */
    public void saveVesselDataBatch(List<VesselData> vesselDataBatch) {
        if (vesselDataBatch.isEmpty()) {
            log.warn("Empty vessel data batch received. No records will be saved.");
            return;
        }
        try {
            // Batch insert VesselData records
            jdbcTemplate.batchUpdate(SQL_INSERT_VESSEL_DATA, vesselDataBatch, vesselDataBatch.size(),
                    (ps, vesselData) -> {
                        ps.setObject(1, vesselData.getVesselDataUuid());
                        ps.setString(2, vesselData.getVesselCode());
                        ps.setTimestamp(3, java.sql.Timestamp.valueOf(vesselData.getDateTime()));
                        ps.setObject(4, vesselData.getLatitude());
                        ps.setObject(5, vesselData.getLongitude());
                        ps.setObject(6, vesselData.getPower());
                        ps.setObject(7, vesselData.getFuelConsumption());
                        ps.setObject(8, vesselData.getActualSpeedOverground());
                        ps.setObject(9, vesselData.getProposedSpeedOverground());
                        ps.setObject(10, vesselData.getPredictedFuelConsumption());
                        ps.setObject(11, vesselData.getSpeedDifference());
                        ps.setObject(12, vesselData.getFuelEfficiency());
                        ps.setObject(13, vesselData.getValidationStatus().name());
                    }
            );

            log.info("Successfully saved batch of {} VesselData records", vesselDataBatch.size());
            // Collect and batch insert validation errors
            saveValidationErrorsBatch(vesselDataBatch);
        } catch (Exception e) {
            log.error("Error during batch insert of vessel data", e);
            throw new RuntimeException("Batch insert failed for vessel data", e);
        }
    }

    /**
     * Saves the associated validation errors for a batch of vessel data.
     *
     * @param vesselDataBatch the list of vessel data whose validation errors will be saved
     */
    private void saveValidationErrorsBatch(List<VesselData> vesselDataBatch) {
        List<Object[]> validationErrorParams = vesselDataBatch.stream()
                .filter(vd -> vd.getValidationErrors() != null && !vd.getValidationErrors().isEmpty())
                .flatMap(vd -> vd.getValidationErrors().stream()
                        .map(error -> new Object[]{
                                error.getVesselData().getVesselDataUuid(),
                                error.getVesselCode(),
                                error.getErrorMessage(),
                                error.getProblemType().name()
                        }))
                .toList();

        if (!validationErrorParams.isEmpty()) {
            jdbcTemplate.batchUpdate(SQL_INSERT_VALIDATION_ERRORS, validationErrorParams);
            log.info("Successfully saved batch of {} validation errors", validationErrorParams.size());
        } else {
            log.info("No validation errors to save in this batch.");
        }
    }

}
