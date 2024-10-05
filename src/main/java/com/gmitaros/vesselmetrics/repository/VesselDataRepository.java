package com.gmitaros.vesselmetrics.repository;

import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VesselDataRepository extends JpaRepository<VesselData, Long> {

    Page<VesselData> findByVesselCodeAndValidationStatus(String vesselCode, ValidationStatus validationStatus, Pageable pageable);

    Page<VesselData> findByVesselCodeAndDateTimeBetween(String vesselCode, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<VesselData> findByVesselCodeAndValidationStatus(String vesselCode, ValidationStatus validationStatus);

    @Query("SELECT COUNT(vd) > 0 FROM VesselData vd WHERE vd.vesselCode = :vesselCode")
    boolean vesselExists(@Param("vesselCode") String vesselCode);

    @Query("SELECT DISTINCT vd.vesselCode FROM VesselData vd")
    List<String> findDistinctVesselCode();
}
