package com.gmitaros.vesselmetrics.util;

import com.gmitaros.vesselmetrics.model.VesselData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilsTest {

    @Test
    void testParseDateTime_ValidFormat() {
        String dateTimeStr = "2023-10-06 12:30:00";
        LocalDateTime expectedDateTime = LocalDateTime.of(2023, 10, 6, 12, 30, 0);

        LocalDateTime actualDateTime = Utils.parseDateTime(dateTimeStr);

        assertEquals(expectedDateTime, actualDateTime);
    }

    @Test
    void testParseDateTime_InvalidFormat() {
        String dateTimeStr = "2023-10-06";

        LocalDateTime actualDateTime = Utils.parseDateTime(dateTimeStr);

        assertNull(actualDateTime);
    }

    @Test
    void testParseDoubleSafe_ValidDouble() {
        String value = "123.45";

        Double actual = Utils.parseDoubleSafe(value);

        assertEquals(123.45, actual);
    }

    @Test
    void testParseDoubleSafe_InvalidDouble() {
        String value = "invalid";

        Double actual = Utils.parseDoubleSafe(value);

        assertNull(actual);
    }

    @Test
    void testMapCsvRecordToVesselData_ValidRecord() throws Exception {
        String csvData = "vessel_code,datetime,latitude,longitude,power,fuel_consumption,actual_speed_overground,proposed_speed_overground,predicted_fuel_consumption\n" +
                "V1234,2023-10-06 12:30:00,12.345,54.321,1200,50,15.5,14.2,45";

        CSVParser csvParser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new StringReader(csvData));
        CSVRecord record = csvParser.getRecords().getFirst();

        VesselData vesselData = Utils.mapCsvRecordToVesselData(record);

        assertNotNull(vesselData);
        assertEquals("V1234", vesselData.getVesselCode());
        assertEquals(LocalDateTime.of(2023, 10, 6, 12, 30, 0), vesselData.getDateTime());
        assertEquals(12.345, vesselData.getLatitude());
        assertEquals(54.321, vesselData.getLongitude());
        assertEquals(1200, vesselData.getPower());
        assertEquals(50, vesselData.getFuelConsumption());
        assertEquals(15.5, vesselData.getActualSpeedOverground());
        assertEquals(14.2, vesselData.getProposedSpeedOverground());
        assertEquals(45, vesselData.getPredictedFuelConsumption());
    }

    @Test
    void testMapCsvRecordToVesselData_InvalidRecord() throws Exception {
        String csvData = "vessel_code,datetime,latitude,longitude,power,fuel_consumption,actual_speed_overground,proposed_speed_overground,predicted_fuel_consumption\n" +
                "V1234,invalid_date,12.345,NULL,1200,50,15.5,14.2,45";

        CSVParser csvParser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new StringReader(csvData));
        CSVRecord record = csvParser.getRecords().getFirst();

        VesselData vesselData = Utils.mapCsvRecordToVesselData(record);

        assertNull(vesselData.getDateTime());
        assertNull(vesselData.getLongitude());
    }
}
