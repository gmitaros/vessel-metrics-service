package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.ValidationError;
import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.model.VesselMetricsStatistics;
import com.gmitaros.vesselmetrics.repository.ValidationErrorRepository;
import com.gmitaros.vesselmetrics.repository.VesselDataRepository;
import com.gmitaros.vesselmetrics.repository.VesselMetricsStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Service
public class OutlierDetectionService {

    private static final Logger log = LoggerFactory.getLogger(OutlierDetectionService.class);

    private final VesselDataRepository vesselDataRepository;
    private final ValidationErrorRepository validationErrorRepository;
    private final VesselMetricsStatisticsRepository vesselMetricsStatisticsRepository;
    private static final int BATCH_SIZE = 10000;

    @Transactional
    public void detectAndStoreOutliers() {
        // Start time for total processing
        long totalStartTime = System.nanoTime();

        Page<VesselData> vesselDataPage;
        List<String> vessels = vesselDataRepository.findDistinctVesselCode();

        for (String vessel : vessels) {
            // Start time for each vessel processing
            long vesselStartTime = System.nanoTime();

            int page = 0;
            do {
                log.info("Fetching vessel data for vessel {} with page {} and batch size {}", vessel, page, BATCH_SIZE);
                vesselDataPage = vesselDataRepository.findByVesselCodeAndValidationStatus(vessel, ValidationStatus.VALID, PageRequest.of(page, BATCH_SIZE));
                log.info("Fetched {} data from total {} for vessel {}", vesselDataPage.getSize(), vesselDataPage.getTotalElements(), vessel);
                List<VesselData> vesselDataList = vesselDataPage.getContent();

                // Apply outlier detection to the current batch
                Optional<VesselMetricsStatistics> statsOpt = vesselMetricsStatisticsRepository.findStatisticsByVesselCode(vessel);
                if (statsOpt.isPresent()) {
                    List<ValidationError> errors = detectOutliersInBatch(statsOpt.get(), vesselDataList);
                    if (!errors.isEmpty()) {
                        log.info("Storing {} outlier errors for vessel {}", errors.size(), vessel);
                        validationErrorRepository.saveAllAndFlush(errors);
                    }
                }

                // Save the batch back to the database
                vesselDataRepository.saveAllAndFlush(vesselDataList);

                page++;
            } while (vesselDataPage.hasNext());

            // End time for each vessel processing
            long vesselEndTime = System.nanoTime();
            long vesselDuration = (vesselEndTime - vesselStartTime) / 1_000_000;
            log.info("Time taken for vessel {}: {} ms", vessel, vesselDuration);
        }

        // End time for total processing
        long totalEndTime = System.nanoTime();
        long totalDuration = (totalEndTime - totalStartTime) / 1_000_000;
        log.info("Total time taken for outlier detection: {} ms", totalDuration);
    }

    private List<ValidationError> detectOutliersInBatch(VesselMetricsStatistics stats, List<VesselData> vesselDataList) {
        ConcurrentLinkedQueue<ValidationError> validationErrorsList = new ConcurrentLinkedQueue<>();
        try (ForkJoinPool customThreadPool = new ForkJoinPool(8)) {
            customThreadPool.submit(() -> {
                vesselDataList.parallelStream().forEach(vesselData -> {
                    List<ValidationError> errors = new ArrayList<>();
                    // Check each metric for outliers
                    checkAndFlagOutlier(errors, vesselData, vesselData.getFuelConsumption(), stats.getAvgFuelConsumption(), stats.getStddevFuelConsumption(), "Fuel consumption is an outlier");
                    checkAndFlagOutlier(errors, vesselData, vesselData.getPower(), stats.getAvgPower(), stats.getStddevPower(), "Power is an outlier");
                    checkAndFlagOutlier(errors, vesselData, vesselData.getActualSpeedOverground(), stats.getAvgActualSpeedOverground(), stats.getStddevActualSpeedOverground(), "Actual speed overground is an outlier");
                    // If any outliers were found, set validation status
                    if (!errors.isEmpty()) {
                        vesselData.setValidationStatus(ValidationStatus.INVALID);
                    }
                    validationErrorsList.addAll(errors);
                });
            }).get();
        } catch (Exception e) {
            log.error("Failed to detect outliers for vessel {}", vesselDataList.getFirst().getVesselCode(), e);
        }
        return new ArrayList<>(validationErrorsList);
    }

    private void checkAndFlagOutlier(List<ValidationError> validationErrors, VesselData vesselData, Double metricValue, Double avgValue, Double stddevValue, String errorMessage) {
        if (metricValue != null && avgValue != null && stddevValue != null) {
            double zScore = calculateZScore(metricValue, avgValue, stddevValue);
            if (isOutlier(zScore)) {
                validationErrors.add(vesselData.addValidationError(errorMessage, ValidationProblemType.OUTLIER));
            }
        }
    }

    private double calculateZScore(double value, double mean, double standardDeviation) {
        return (value - mean) / standardDeviation;
    }

    private boolean isOutlier(double zScore) {
        return Math.abs(zScore) > 3;
    }
}
