package com.gmitaros.vesselmetrics.repository;

import com.gmitaros.vesselmetrics.dto.ValidationIssueDTO;
import com.gmitaros.vesselmetrics.model.ValidationError;
import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationErrorRepository extends JpaRepository<ValidationError, Long> {

    @Query("SELECT new com.gmitaros.vesselmetrics.dto.ValidationIssueDTO(ve.problemType, COUNT(ve)) " +
            "FROM ValidationError ve " +
            "WHERE ve.vesselCode = :vesselCode " +
            "GROUP BY ve.problemType " +
            "ORDER BY COUNT(ve) DESC")
    List<ValidationIssueDTO> findValidationIssuesByVesselCode(@Param("vesselCode") String vesselCode);


    @Query("SELECT ve.vesselData.vesselDataUuid " +
            "FROM ValidationError ve " +
            "WHERE ve.vesselCode = :vesselCode AND ve.problemType = :problemType")
    List<String> findVesselDataUuidsByVesselCodeAndProblemType(
            @Param("vesselCode") String vesselCode,
            @Param("problemType") ValidationProblemType problemType);


}
