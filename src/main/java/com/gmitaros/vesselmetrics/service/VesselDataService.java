package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.dto.ComplianceDTO;
import com.gmitaros.vesselmetrics.dto.SpeedDifferenceDTO;
import com.gmitaros.vesselmetrics.dto.ValidationIssueDTO;
import com.gmitaros.vesselmetrics.dto.VesselDataDTO;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.repository.ValidationErrorRepository;
import com.gmitaros.vesselmetrics.repository.VesselDataRepository;
import com.gmitaros.vesselmetrics.util.Utils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class responsible for handling operations related to vessel data.
 * It provides functionality for calculating speed differences, validation issues, compliance, and merged data retrieval.
 */
@Service
@RequiredArgsConstructor
public class VesselDataService {

    private static final Logger log = LoggerFactory.getLogger(VesselDataService.class);

    private final VesselDataRepository vesselDataRepository;
    private final ValidationErrorRepository validationErrorRepository;

    /**
     * Retrieves a paginated list of speed differences for the specified vessel.
     * Logs the request and throws an exception if the vessel doesn't exist.
     *
     * @param vesselCode the unique identifier of the vessel
     * @param pageable   pagination details
     * @return a page of SpeedDifferenceDTO containing speed differences
     */
    @Transactional(readOnly = true)
    public Page<SpeedDifferenceDTO> getSpeedDifferences(String vesselCode, Pageable pageable) {
        log.info("Fetching speed differences for vessel: {}", vesselCode);

        if (!vesselDataRepository.vesselExists(vesselCode)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode + " does not exist.");
        }

        Page<VesselData> dataList = vesselDataRepository.findByVesselCodeAndValidationStatus(vesselCode, ValidationStatus.VALID, pageable);
        log.info("Successfully fetched speed differences for vessel: {}", vesselCode);
        return dataList.map(data -> new SpeedDifferenceDTO(
                data.getDateTime(),
                data.getLatitude(),
                data.getLongitude(),
                data.getSpeedDifference()
        ));
    }

    /**
     * Retrieves validation issues for the specified vessel, sorted by frequency.
     * Logs the request and throws an exception if the vessel doesn't exist.
     *
     * @param vesselCode the unique identifier of the vessel
     * @return a list of validation issues with their occurrence frequency
     */
    @Transactional(readOnly = true)
    public List<ValidationIssueDTO> getValidationIssues(String vesselCode) {
        log.info("Fetching validation issues for vessel: {}", vesselCode);
        if (!vesselDataRepository.vesselExists(vesselCode)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode + " does not exist.");
        }
        log.info("Successfully fetched validation issues for vessel: {}", vesselCode);
        return validationErrorRepository.findValidationIssuesByVesselCode(vesselCode);
    }

    /**
     * Calculates the compliance percentage for the specified vessel based on how far its actual speed
     * deviates from the proposed speed.
     *
     * @param vesselCode the unique identifier of the vessel
     * @return a ComplianceDTO containing the compliance percentage for the vessel
     */
    @Transactional(readOnly = true)
    public ComplianceDTO calculateCompliance(String vesselCode) {
        log.info("Calculating compliance for vessel: {}", vesselCode);
        List<VesselData> dataList = vesselDataRepository.findByVesselCodeAndValidationStatus(vesselCode, ValidationStatus.VALID);

        double totalCompliance = 0.0;
        int count = 0;

        for (VesselData data : dataList) {
            Double actualSpeed = data.getActualSpeedOverground();
            Double proposedSpeed = data.getProposedSpeedOverground();
            if (actualSpeed != null && proposedSpeed != null && proposedSpeed != 0) {
                double compliance = (1 - Math.abs(actualSpeed - proposedSpeed) / proposedSpeed) * 100;
                totalCompliance += compliance;
                count++;
            }
        }
        double averageCompliance = count > 0 ? totalCompliance / count : 0;
        log.info("Compliance for vessel {} calculated as {}%", vesselCode, averageCompliance);
        return new ComplianceDTO(vesselCode, averageCompliance);
    }

    @Transactional(readOnly = true)
    public Page<VesselDataDTO> getMergedData(String vesselCode, String startDate, String endDate, Pageable pageable) {
        log.info("Fetching merged data for vessel: {} from {} to {}", vesselCode, startDate, endDate);
        final LocalDateTime start = LocalDateTime.parse(startDate);
        final LocalDateTime end = LocalDateTime.parse(endDate);
        final Page<VesselData> dataList = vesselDataRepository.findByVesselCodeAndDateTimeBetween(vesselCode, start, end, pageable);
        log.info("Successfully fetched merged data for vessel: {}", vesselCode);
        return dataList.map(Utils::mapToVesselDataDTO);
    }

}
