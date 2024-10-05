package com.gmitaros.vesselmetrics.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@Table(name = "vessel_data")
@AllArgsConstructor
@Getter
@Setter
public class VesselData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "vessel_data_uuid", nullable = false, unique = true)
    private String vesselDataUuid;
    private String vesselCode;
    private LocalDateTime dateTime;
    private Double latitude;
    private Double longitude;
    private Double power;
    private Double fuelConsumption;
    private Double actualSpeedOverground;
    private Double proposedSpeedOverground;
    private Double predictedFuelConsumption;
    private Double speedDifference;
    private Double fuelEfficiency;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false)
    private ValidationStatus validationStatus;

    @OneToMany(mappedBy = "vesselData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ValidationError> validationErrors = new ArrayList<>();

    public ValidationError addValidationError(String errorMessage, ValidationProblemType problemType) {
        ValidationError error = new ValidationError(this, errorMessage, problemType);
        validationErrors.add(error);
        return error;
    }

}
