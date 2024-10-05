package com.gmitaros.vesselmetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComplianceComparisonResponseDTO {
    private String vesselCode1;
    private double compliance1;
    private String vesselCode2;
    private double compliance2;
    private String result;
}
