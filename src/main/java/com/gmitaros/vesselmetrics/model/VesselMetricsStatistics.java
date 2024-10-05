package com.gmitaros.vesselmetrics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@NoArgsConstructor
@Table(name = "vessel_metrics_statistics")
@AllArgsConstructor
@Getter
@Setter
public class VesselMetricsStatistics {

    @Id
    private String vesselCode;
    private Double avgPower;
    private Double stddevPower;
    private Double avgFuelConsumption;
    private Double stddevFuelConsumption;
    private Double avgActualSpeedOverground;
    private Double stddevActualSpeedOverground;
    private Double avgProposedSpeedOverground;
    private Double stddevProposedSpeedOverground;
    private Double avgPredictedFuelConsumption;
    private Double stddevPredictedFuelConsumption;
    private Double avgSpeedDifference;
    private Double stddevSpeedDifference;
    private Double avgFuelEfficiency;
    private Double stddevFuelEfficiency;

}
