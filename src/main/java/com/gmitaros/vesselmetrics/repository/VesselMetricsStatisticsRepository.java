package com.gmitaros.vesselmetrics.repository;

import com.gmitaros.vesselmetrics.model.VesselMetricsStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VesselMetricsStatisticsRepository extends JpaRepository<VesselMetricsStatistics, String> {

    @Query("SELECT v FROM VesselMetricsStatistics v WHERE v.vesselCode = :vesselCode")
    Optional<VesselMetricsStatistics> findStatisticsByVesselCode(String vesselCode);
}
