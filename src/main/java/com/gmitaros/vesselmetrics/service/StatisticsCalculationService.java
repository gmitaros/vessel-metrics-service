package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.dto.ProblematicWaypointGroupDTO;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.repository.ValidationErrorRepository;
import com.gmitaros.vesselmetrics.repository.VesselDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating statistics related to vessel data,
 * including identifying groups of problematic waypoints based on validation errors.
 */
@Service
@RequiredArgsConstructor
public class StatisticsCalculationService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsCalculationService.class);

    private final VesselDataRepository vesselDataRepository;
    private final ValidationErrorRepository validationErrorRepository;

    /**
     * Retrieves groups of consecutive waypoints with validation problems for a given vessel, filtered by problem type.
     *
     * @param vesselCode  The vessel code to search for.
     * @param problemType The type of problem to filter by, or null for all problem types.
     * @return A list of {@link ProblematicWaypointGroupDTO} containing consecutive waypoints with problems.
     * @throws VesselNotFoundException if the specified vessel does not exist.
     */
    @Transactional(readOnly = true)
    public List<ProblematicWaypointGroupDTO> getProblematicWaypointGroups(String vesselCode, ValidationProblemType problemType) {
        log.info("Retrieving problematic waypoints for vessel: {}", vesselCode);
        if (!vesselDataRepository.vesselExists(vesselCode)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode + " does not exist.");
        }

        List<VesselData> dataList = vesselDataRepository.findByVesselCodeAndValidationStatus(vesselCode, ValidationStatus.INVALID);

        Map<String, VesselData> vesselDataMap = dataList.stream().collect(Collectors.toMap(VesselData::getVesselDataUuid, data -> data));

        List<VesselData> filteredDataList = getFilteredDataList(vesselCode, problemType, vesselDataMap, dataList);

        // Group consecutive problematic waypoints
        return groupConsecutiveProblematicWaypoints(filteredDataList);
    }

    private List<VesselData> getFilteredDataList(String vesselCode, ValidationProblemType problemType, Map<String, VesselData> vesselDataMap, List<VesselData> dataList) {
        if (problemType != null) {
            // Fetch ValidationErrors matching the vesselCode and problemType
            List<String> vesselDataUuids = validationErrorRepository.findVesselDataUuidsByVesselCodeAndProblemType(vesselCode, problemType);

            // Filter dataList based on the vesselDataUuids
            return vesselDataUuids.stream()
                    .map(vesselDataMap::get)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(VesselData::getDateTime))
                    .collect(Collectors.toList());
        } else {
            // No problem type specified; use all invalid data
            return dataList.stream()
                    .sorted(Comparator.comparing(VesselData::getDateTime))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Groups consecutive problematic waypoints from a list of vessel data.
     *
     * @param dataList The list of vessel data to process.
     * @return A list of {@link ProblematicWaypointGroupDTO} representing consecutive problematic waypoints.
     */
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

    /**
     * Checks if two vessel data points are consecutive based on the timestamp.
     *
     * @param data1 First vessel data.
     * @param data2 Second vessel data.
     * @return True if the two data points are consecutive, false otherwise.
     */
    private boolean isConsecutive(VesselData data1, VesselData data2) {
        long timeDifferenceInSeconds = java.time.Duration.between(data1.getDateTime(), data2.getDateTime()).getSeconds();
        return timeDifferenceInSeconds <= 60 && timeDifferenceInSeconds >= 0;
    }

}