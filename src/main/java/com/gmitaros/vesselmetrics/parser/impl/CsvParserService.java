package com.gmitaros.vesselmetrics.parser.impl;

import com.gmitaros.vesselmetrics.model.VesselData;
import com.gmitaros.vesselmetrics.parser.DataParser;
import com.gmitaros.vesselmetrics.repository.VesselDataRepository;
import com.gmitaros.vesselmetrics.service.MetricsCalculationService;
import com.gmitaros.vesselmetrics.service.OutlierDetectionService;
import com.gmitaros.vesselmetrics.service.ValidationService;
import com.gmitaros.vesselmetrics.service.VesselDataBatchService;
import com.gmitaros.vesselmetrics.util.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${vessel.metrics.csv.load.if.already.have.data:false}")
    private boolean loadCsvIfAlreadyHaveData;

    @Value("${vessel.metrics.csv.path}")
    private String vesselDataPath;

    private final VesselDataRepository vesselDataRepository;
    private final VesselDataBatchService vesselDataBatchService;
    private final OutlierDetectionService outlierDetectionService;
    private final ValidationService validationService;
    private final MetricsCalculationService metricsCalculationService;

    /**
     * Listener for when the application is fully initialized and ready.
     * It will trigger CSV parsing and data loading if required.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        long vesselData = vesselDataRepository.count();
        log.info("Found {} vessel data in vessel_data db ", vesselData);
        boolean loadData = vesselData <= 0 || loadCsvIfAlreadyHaveData;
        if (loadData) {
            log.info("CsvParserService will load data from {} file", vesselDataPath);
            try (InputStream inputStream = getClass().getResourceAsStream(vesselDataPath)) {
                parseAndSave(inputStream);
                checkForOutliers();
            } catch (Exception e) {
                log.error("Error initializing data: ", e);
                throw new RuntimeException("Failed to initialize data", e);
            }
        } else {
            log.info("Skipping loading again vessel data from CSV file");
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
