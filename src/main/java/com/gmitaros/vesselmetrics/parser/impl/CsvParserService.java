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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

/**
 * Service responsible for parsing vessel data from a CSV file, validating and calculating metrics,
 * and storing the data in batches. It also detects outliers after the data has been processed.
 */
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

    /**
     * Initialization method that triggers the parsing of the CSV file and outlier detection.
     * Called once the service is constructed.
     */
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

    /**
     * Parses the CSV file and processes the records, saving them in batches.
     *
     * @param inputStream the input stream of the CSV file to be parsed
     */
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
                    .map(Utils::mapCsvRecordToVesselData)
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

}
