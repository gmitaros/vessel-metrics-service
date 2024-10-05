package com.gmitaros.vesselmetrics.dto;

import com.gmitaros.vesselmetrics.model.VesselData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ProblematicWaypointGroupDTO {

    private int problemCount;
    private List<VesselDataDTO> waypoints;

    public ProblematicWaypointGroupDTO(int problemCount, List<VesselData> vesselDataList) {
        this.problemCount = problemCount;

        this.waypoints = vesselDataList.stream()
                .map(VesselDataDTO::from)
                .collect(Collectors.toList());
    }
}
