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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vessels")
@RequiredArgsConstructor
public class VesselDataController {

    private final VesselDataService vesselDataService;
    private final VesselComplianceService vesselComplianceService;
    private final StatisticsCalculationService statisticsCalculationService;

    @GetMapping("/{vesselCode}/speed-differences")
    public ResponseEntity<List<SpeedDifferenceDTO>> getSpeedDifferences(@PathVariable String vesselCode) {
        List<SpeedDifferenceDTO> differences = vesselDataService.getSpeedDifferences(vesselCode);
        return ResponseEntity.ok(differences);
    }

    @GetMapping("/{vesselCode}/validation-issues")
    public ResponseEntity<List<ValidationIssueDTO>> getValidationIssues(@PathVariable String vesselCode) {
        List<ValidationIssueDTO> issues = vesselDataService.getValidationIssues(vesselCode);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/compare-compliance")
    public ResponseEntity<ComplianceComparisonResponseDTO> compareVesselsCompliance(
            @RequestParam String vesselCode1,
            @RequestParam String vesselCode2) {
        ComplianceComparisonResponseDTO complianceComparisonResponse = vesselComplianceService.compareVesselCompliance(vesselCode1, vesselCode2);
        return ResponseEntity.ok(complianceComparisonResponse);
    }

    @GetMapping("/{vesselCode}/data-merge")
    public ResponseEntity<List<VesselDataDTO>> getDataMerge(
            @PathVariable String vesselCode,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<VesselDataDTO> mergedData = vesselDataService.getMergedData(vesselCode, startDate, endDate);
        return ResponseEntity.ok(mergedData);
    }

    @GetMapping("/{vesselCode}/problematic-waypoints")
    public ResponseEntity<List<ProblematicWaypointGroupDTO>> getProblematicWaypoints(
            @PathVariable String vesselCode,
            @RequestParam(required = false) ValidationProblemType problemType) {
        List<ProblematicWaypointGroupDTO> waypointGroups = statisticsCalculationService.getProblematicWaypointGroups(vesselCode, problemType);
        return ResponseEntity.ok(waypointGroups);
    }



}
