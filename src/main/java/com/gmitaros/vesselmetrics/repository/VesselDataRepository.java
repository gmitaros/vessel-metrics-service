package com.gmitaros.vesselmetrics.repository;

import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VesselDataRepository extends JpaRepository<VesselData, Long> {

    List<VesselData> findByVesselCodeAndValidationStatus(String vesselCode, ValidationStatus validationStatus);

    List<VesselData> findByVesselCodeAndDateTimeBetween(String vesselCode, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(vd) > 0 FROM VesselData vd WHERE vd.vesselCode = :vesselCode")
    boolean vesselExists(@Param("vesselCode") String vesselCode);

}
