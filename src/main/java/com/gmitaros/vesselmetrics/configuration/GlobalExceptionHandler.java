package com.gmitaros.vesselmetrics.configuration;

import com.gmitaros.vesselmetrics.dto.ErrorResponseDTO;
import com.gmitaros.vesselmetrics.exception.ComplianceCalculationException;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(VesselNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleVesselNotFoundException(VesselNotFoundException ex) {
        log.error("Vessel not found: ", ex);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Vessel Not Found", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ComplianceCalculationException.class)
    public ResponseEntity<ErrorResponseDTO> handleComplianceCalculationException(ComplianceCalculationException ex) {
        log.error("Error during compliance calculation: ", ex);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("Compliance Calculation Error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("An unexpected error occurred", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
