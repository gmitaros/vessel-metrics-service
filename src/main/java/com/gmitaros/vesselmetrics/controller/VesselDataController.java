package com.gmitaros.vesselmetrics.controller;

import com.gmitaros.vesselmetrics.dto.ComplianceComparisonResponseDTO;
import com.gmitaros.vesselmetrics.dto.ProblematicWaypointGroupDTO;
import com.gmitaros.vesselmetrics.dto.SpeedDifferenceDTO;
import com.gmitaros.vesselmetrics.dto.ValidationIssueDTO;
import com.gmitaros.vesselmetrics.dto.VesselDataDTO;
import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import com.gmitaros.vesselmetrics.service.StatisticsCalculationService;
import com.gmitaros.vesselmetrics.service.VesselComplianceService;
import com.gmitaros.vesselmetrics.service.VesselDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing vessel-related data operations.
 * Provides endpoints to retrieve speed differences, validation issues,
 * compare compliance between vessels, merge vessel data, and identify
 * problematic waypoints.
 * <p>
 * This controller interacts with the {@link VesselDataService}, {@link VesselComplianceService},
 * and {@link StatisticsCalculationService} to handle the business logic.
 *
 * <p>
 * Includes error handling, pagination for large datasets, and optional filtering based on validation problems.
 * </p>
 *
 * @author George
 */
@RestController
@RequestMapping("/vessels")
@RequiredArgsConstructor
public class VesselDataController {

    private final VesselDataService vesselDataService;
    private final VesselComplianceService vesselComplianceService;
    private final StatisticsCalculationService statisticsCalculationService;

    /**
     * Retrieves the speed differences between the vessel's actual speed and proposed speed over ground.
     * Supports pagination for large datasets.
     *
     * @param vesselCode the unique code of the vessel
     * @param pageable   pagination information
     * @return the paginated list of speed differences
     */
    @GetMapping("/{vesselCode}/speed-differences")
    public ResponseEntity<Page<SpeedDifferenceDTO>> getSpeedDifferences(
            @PathVariable String vesselCode,
            Pageable pageable) {

        Page<SpeedDifferenceDTO> differences = vesselDataService.getSpeedDifferences(vesselCode, pageable);
        return ResponseEntity.ok(differences);
    }

    /**
     * Retrieves validation issues for a specified vessel.
     * The issues are sorted by frequency of occurrence, descending.
     *
     * @param vesselCode the unique code of the vessel
     * @return the list of validation issues sorted by frequency
     */
    @GetMapping("/{vesselCode}/validation-issues")
    public ResponseEntity<List<ValidationIssueDTO>> getValidationIssues(@PathVariable String vesselCode) {
        List<ValidationIssueDTO> issues = vesselDataService.getValidationIssues(vesselCode);
        return ResponseEntity.ok(issues);
    }

    /**
     * Compares the compliance of two vessels with the system's suggestions.
     * The comparison is based on how far each vessel's actual speed was from the proposed speed.
     *
     * @param vesselCode1 the first vessel's code
     * @param vesselCode2 the second vessel's code
     * @return a DTO containing the compliance comparison result
     */
    @GetMapping("/compare-compliance")
    public ResponseEntity<ComplianceComparisonResponseDTO> compareVesselsCompliance(
            @RequestParam String vesselCode1,
            @RequestParam String vesselCode2) {
        ComplianceComparisonResponseDTO complianceComparisonResponse = vesselComplianceService.compareVesselCompliance(vesselCode1, vesselCode2);
        return ResponseEntity.ok(complianceComparisonResponse);
    }

    /**
     * Retrieves all values for both raw and calculated metrics for a specified period and vessel.
     * Supports pagination for large datasets.
     *
     * @param vesselCode the unique code of the vessel
     * @param startDate  the start date of the period (ISO format)
     * @param endDate    the end date of the period (ISO format)
     * @param pageable   pagination information
     * @return the paginated list of merged vessel data
     */
    @GetMapping("/{vesselCode}/data-merge")
    public ResponseEntity<Page<VesselDataDTO>> getDataMerge(
            @PathVariable String vesselCode,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Pageable pageable) {
        Page<VesselDataDTO> mergedData = vesselDataService.getMergedData(vesselCode, startDate, endDate, pageable);
        return ResponseEntity.ok(mergedData);
    }

    /**
     * Identifies groups of consecutive waypoints with problematic data for a specific vessel.
     * Allows optional filtering by a specific problem type.
     *
     * @param vesselCode  the unique code of the vessel
     * @param problemType the type of problem to filter by (optional)
     * @return a list of grouped problematic waypoints, sorted by the number of problems found
     */
    @GetMapping("/{vesselCode}/problematic-waypoints")
    public ResponseEntity<List<ProblematicWaypointGroupDTO>> getProblematicWaypoints(
            @PathVariable String vesselCode,
            @RequestParam(required = false) ValidationProblemType problemType) {
        List<ProblematicWaypointGroupDTO> waypointGroups = statisticsCalculationService.getProblematicWaypointGroups(vesselCode, problemType);
        return ResponseEntity.ok(waypointGroups);
    }


}
