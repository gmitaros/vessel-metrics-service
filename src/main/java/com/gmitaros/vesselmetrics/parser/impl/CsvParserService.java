package com.gmitaros.vesselmetrics.parser.impl;

import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.parser.DataParser;
import com.gmitaros.vesselmetrics.service.MetricsCalculationService;
import com.gmitaros.vesselmetrics.service.OutlierDetectionService;
import com.gmitaros.vesselmetrics.service.ValidationService;
import com.gmitaros.vesselmetrics.service.VesselDataBatchService;
import com.gmitaros.vesselmetrics.util.Utils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
@ConditionalOnProperty(name = "vessel.metrics.csv.load", havingValue = "true")
public class CsvParserService implements DataParser {

    private static final Logger log = LoggerFactory.getLogger(CsvParserService.class);
    private static final int LOG_INTERVAL = 1000;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    private final VesselDataBatchService vesselDataBatchService;
    private final OutlierDetectionService outlierDetectionService;
    private final ValidationService validationService;
    private final MetricsCalculationService metricsCalculationService;

    @PostConstruct
    public void init() {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/vessel_data.csv")) {
            parseAndSave(inputStream);
            checkForOutliers();
        } catch (Exception e) {
            log.error("Error initializing data: ", e);
            throw new RuntimeException("Failed to initialize data", e);
        }
    }

    public void parseAndSave(InputStream inputStream) {
        long startTime = System.currentTimeMillis();

        try {
            Reader reader = new InputStreamReader(inputStream);
            CSVParser csvParser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            List<VesselData> batch = new ArrayList<>();
            AtomicInteger totalRecordsProcessed = new AtomicInteger();

            StreamSupport.stream(csvParser.spliterator(), false)
                    .map(this::mapCsvRecordToVesselData)
                    .filter(Objects::nonNull)
                    .peek(validationService::validate)
                    .peek(metricsCalculationService::calculateMetrics)
                    .forEach(data -> {
                        batch.add(data);
                        int count = totalRecordsProcessed.incrementAndGet();

                        if (count % LOG_INTERVAL == 0) {
                            log.info("Processed {} records so far...", count);
                        }

                        if (batch.size() >= batchSize) {
                            vesselDataBatchService.saveVesselDataBatch(new ArrayList<>(batch));
                            batch.clear();
                        }
                    });

            if (!batch.isEmpty()) {
                vesselDataBatchService.saveVesselDataBatch(new ArrayList<>(batch));
            }

            log.info("Finished processing. Total records processed: {}", totalRecordsProcessed.get());

        } catch (Exception e) {
            log.error("Error while parsing CSV file: ", e);
            throw new RuntimeException("Failed to parse CSV file", e);
        } finally {
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            log.info("CSV processing completed in {} ms", elapsedTime);
        }
    }

    private void checkForOutliers() {
        log.info("Starting check for outliers");
        outlierDetectionService.detectAndStoreOutliers();
        log.info("Check for outliers finished");
    }

    private VesselData mapCsvRecordToVesselData(CSVRecord record) {
        try {
            final String vesselCode = record.get("vessel_code");
            final String dateTime = record.get("datetime");
            final Double latitude = validationService.parseDoubleSafe(record.get("latitude"));
            final Double longitude = validationService.parseDoubleSafe(record.get("longitude"));
            final Double power = validationService.parseDoubleSafe(record.get("power"));
            final Double fuelConsumption = validationService.parseDoubleSafe(record.get("fuel_consumption"));
            final Double actualSpeedOverground = validationService.parseDoubleSafe(record.get("actual_speed_overground"));
            final Double proposedSpeedOverground = validationService.parseDoubleSafe(record.get("proposed_speed_overground"));
            final Double predictedFuelConsumption = validationService.parseDoubleSafe(record.get("predicted_fuel_consumption"));

            return VesselData.builder()
                    .vesselDataUuid(UUID.randomUUID().toString())
                    .vesselCode(vesselCode)
                    .dateTime(Utils.parseDateTime(dateTime))
                    .latitude(latitude)
                    .longitude(longitude)
                    .power(power)
                    .fuelConsumption(fuelConsumption)
                    .actualSpeedOverground(actualSpeedOverground)
                    .proposedSpeedOverground(proposedSpeedOverground)
                    .predictedFuelConsumption(predictedFuelConsumption)
                    .validationErrors(new ArrayList<>())
                    .build();

        } catch (Exception e) {
            log.error("Error mapping CSV record: {}", e.getMessage());
            return null;
        }
    }
}
