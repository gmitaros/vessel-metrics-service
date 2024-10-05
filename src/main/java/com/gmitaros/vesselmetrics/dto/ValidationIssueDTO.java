package com.gmitaros.vesselmetrics.dto;

import com.gmitaros.vesselmetrics.model.ValidationProblemType;

public record ValidationIssueDTO(ValidationProblemType issue, long frequency) {
}
