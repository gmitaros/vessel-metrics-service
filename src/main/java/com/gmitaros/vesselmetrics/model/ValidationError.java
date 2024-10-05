package com.gmitaros.vesselmetrics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vessel_data_validation_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vessel_data_uuid", referencedColumnName = "vessel_data_uuid")
    private VesselData vesselData;

    @Column(name = "vessel_code", nullable = false)
    private String vesselCode;

    @Column(name = "error_message", nullable = false)
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", nullable = false)
    private ValidationProblemType problemType;


    public ValidationError(VesselData vesselData, String errorMessage, ValidationProblemType problemType) {
        this.vesselData = vesselData;
        this.vesselCode = vesselData.getVesselCode();
        this.errorMessage = errorMessage;
        this.problemType = problemType;
    }
}
