package com.gmitaros.vesselmetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDTO {
    private String errorMessage;
    private String details;
}
