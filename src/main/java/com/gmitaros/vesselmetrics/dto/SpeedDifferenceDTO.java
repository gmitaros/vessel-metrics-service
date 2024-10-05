package com.gmitaros.vesselmetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SpeedDifferenceDTO {
    private LocalDateTime dateTime;
    private Double latitude;
    private Double longitude;
    private Double speedDifference;
}
