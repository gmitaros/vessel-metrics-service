package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.dto.ComplianceComparisonResponseDTO;
import com.gmitaros.vesselmetrics.dto.ComplianceDTO;
import com.gmitaros.vesselmetrics.exception.ComplianceCalculationException;
import com.gmitaros.vesselmetrics.exception.VesselNotFoundException;
import com.gmitaros.vesselmetrics.repository.VesselDataRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class VesselComplianceService {

    private static final Logger log = LoggerFactory.getLogger(VesselComplianceService.class);

    private final VesselDataRepository vesselDataRepository;
    private final VesselDataService vesselDataService;
    private final ExecutorService executorService;

    @Transactional(readOnly = true)
    public ComplianceComparisonResponseDTO compareVesselCompliance(String vesselCode1, String vesselCode2) {
        long startTime = System.currentTimeMillis();
        if (!vesselDataRepository.vesselExists(vesselCode1)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode1 + " does not exist.");
        }

        if (!vesselDataRepository.vesselExists(vesselCode2)) {
            throw new VesselNotFoundException("Vessel with code " + vesselCode2 + " does not exist.");
        }

        try {
            log.info("Starting compliance calculation for vessels {} and {}", vesselCode1, vesselCode2);

            CompletableFuture<ComplianceDTO> futureCompliance1 = CompletableFuture.supplyAsync(() ->
                    vesselDataService.calculateCompliance(vesselCode1), executorService);

            CompletableFuture<ComplianceDTO> futureCompliance2 = CompletableFuture.supplyAsync(() ->
                    vesselDataService.calculateCompliance(vesselCode2), executorService);

            CompletableFuture.allOf(futureCompliance1, futureCompliance2).join();

            ComplianceDTO compliance1 = futureCompliance1.get();
            ComplianceDTO compliance2 = futureCompliance2.get();
            long endTime = System.currentTimeMillis();
            log.info("Compliance calculation completed in {} ms", (endTime - startTime));
            String result = getComplianceResult(vesselCode1, vesselCode2, compliance1, compliance2);
            log.info("Compliance calculation completed for vessels {} and {}", vesselCode1, vesselCode2);
            return new ComplianceComparisonResponseDTO(
                    vesselCode1, compliance1.compliancePercentage(),
                    vesselCode2, compliance2.compliancePercentage(),
                    result);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while calculating compliance", e);
            Thread.currentThread().interrupt();
            throw new ComplianceCalculationException("An error occurred while calculating compliance.", e);
        }
    }

    private static String getComplianceResult(String vesselCode1, String vesselCode2, ComplianceDTO compliance1, ComplianceDTO compliance2) {
        String result;
        if (compliance1.compliancePercentage() > compliance2.compliancePercentage()) {
            result = vesselCode1 + " is more compliant.";
        } else if (compliance1.compliancePercentage() < compliance2.compliancePercentage()) {
            result = vesselCode2 + " is more compliant.";
        } else {
            result = "Both vessels have equal compliance.";
        }
        return result;
    }


}
