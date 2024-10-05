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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VesselDataService {

    private final VesselDataRepository vesselDataRepository;
    private final ValidationErrorRepository validationErrorRepository;

    @Transactional(readOnly = true)
    public Page<SpeedDifferenceDTO> getSpeedDifferences(String vesselCode, Pageable pageable) {
        if (!vesselDataRepository.vesselExists(vesselCode)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode + " does not exist.");
        }

        Page<VesselData> dataList = vesselDataRepository.findByVesselCodeAndValidationStatus(vesselCode, ValidationStatus.VALID, pageable);

        return dataList.map(data -> new SpeedDifferenceDTO(
                data.getDateTime(),
                data.getLatitude(),
                data.getLongitude(),
                data.getSpeedDifference()
        ));
    }

    @Transactional(readOnly = true)
    public List<ValidationIssueDTO> getValidationIssues(String vesselCode) {
        if (!vesselDataRepository.vesselExists(vesselCode)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode + " does not exist.");
        }
        return validationErrorRepository.findValidationIssuesByVesselCode(vesselCode);
    }

    @Transactional(readOnly = true)
    public ComplianceDTO calculateCompliance(String vesselCode) {
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
        return new ComplianceDTO(vesselCode, averageCompliance);
    }

    @Transactional(readOnly = true)
    public Page<VesselDataDTO> getMergedData(String vesselCode, String startDate, String endDate, Pageable pageable) {
        final LocalDateTime start = LocalDateTime.parse(startDate);
        final LocalDateTime end = LocalDateTime.parse(endDate);
        final Page<VesselData> dataList = vesselDataRepository.findByVesselCodeAndDateTimeBetween(vesselCode, start, end, pageable);
        return dataList.map(Utils::mapToVesselDataDTO);
    }

}
