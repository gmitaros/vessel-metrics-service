package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.dto.ProblematicWaypointGroupDTO;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.repository.ValidationErrorRepository;
import com.gmitaros.vesselmetrics.repository.VesselDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsCalculationService {

    private final VesselDataRepository vesselDataRepository;
    private final ValidationErrorRepository validationErrorRepository;

    @Transactional(readOnly = true)
    public List<ProblematicWaypointGroupDTO> getProblematicWaypointGroups(String vesselCode, ValidationProblemType problemType) {
        // Validate vessel existence
        if (!vesselDataRepository.vesselExists(vesselCode)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode + " does not exist.");
        }

        // Fetch vessel data with INVALID status for the given vesselCode
        List<VesselData> dataList = vesselDataRepository.findByVesselCodeAndValidationStatus(vesselCode, ValidationStatus.INVALID);

        // Map VesselData UUIDs to their respective ValidationErrors
        Map<String, VesselData> vesselDataMap = dataList.stream()
                .collect(Collectors.toMap(VesselData::getVesselDataUuid, data -> data));

        // Fetch ValidationErrors for the vessel and problemType
        List<String> validProblemTypes = getValidProblemTypes();

        List<VesselData> filteredDataList;

        if (problemType != null) {
            if (!validProblemTypes.contains(problemType.name())) {
                throw new IllegalArgumentException("Invalid problem type specified.");
            }

            // Fetch ValidationErrors matching the vesselCode and problemType
            List<String> vesselDataUuids = validationErrorRepository.findVesselDataUuidsByVesselCodeAndProblemType(vesselCode, problemType);

            // Filter dataList based on the vesselDataUuids
            filteredDataList = vesselDataUuids.stream()
                    .map(vesselDataMap::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(VesselData::getDateTime))
                    .collect(Collectors.toList());
        } else {
            // No problem type specified; use all invalid data
            filteredDataList = dataList.stream()
                    .sorted(Comparator.comparing(VesselData::getDateTime))
                    .collect(Collectors.toList());
        }

        // Group consecutive problematic waypoints
        return groupConsecutiveProblematicWaypoints(filteredDataList);
    }

    private List<ProblematicWaypointGroupDTO> groupConsecutiveProblematicWaypoints(List<VesselData> dataList) {
        List<ProblematicWaypointGroupDTO> groups = new ArrayList<>();
        List<VesselData> currentGroup = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            VesselData currentData = dataList.get(i);

            currentGroup.add(currentData);

            // If this is the last data point or the next data point isn't consecutive, save the group
            if (i == dataList.size() - 1 || !isConsecutive(currentData, dataList.get(i + 1))) {
                groups.add(new ProblematicWaypointGroupDTO(currentGroup.size(), new ArrayList<>(currentGroup)));
                currentGroup.clear();
            }
        }

        // Sort the groups by the number of problems found, descending
        groups.sort(Comparator.comparingInt(ProblematicWaypointGroupDTO::getProblemCount).reversed());

        return groups;
    }

    // Check if two data points are consecutive
    private boolean isConsecutive(VesselData data1, VesselData data2) {
        long timeDifferenceInSeconds = java.time.Duration.between(data1.getDateTime(), data2.getDateTime()).getSeconds();
        return timeDifferenceInSeconds <= 60 && timeDifferenceInSeconds >= 0;
    }

    // Get list of valid problem types from the enum
    private List<String> getValidProblemTypes() {
        return Arrays.stream(ValidationProblemType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}