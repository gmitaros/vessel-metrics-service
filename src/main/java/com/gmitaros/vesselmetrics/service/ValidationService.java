package com.gmitaros.vesselmetrics.service;

import com.gmitaros.vesselmetrics.model.ValidationProblemType;
import com.gmitaros.vesselmetrics.model.ValidationStatus;
import com.gmitaros.vesselmetrics.model.VesselData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    @Value("${vesselmetrics.outlier.threshold:0.01}")
    private double outlierThreshold;

    public Double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void validate(VesselData data) {
        boolean isValid = true;

        if (data.getVesselCode() == null || data.getVesselCode().isEmpty()) {
            data.addValidationError("Missing vessel code", ValidationProblemType.MISSING_VESSEL_CODE);
            isValid = false;
        }
        if (data.getDateTime() == null) {
            data.addValidationError("Missing date-time", ValidationProblemType.MISSING_DATETIME);
            isValid = false;
        }
        if (data.getLatitude() == null) {
            data.addValidationError("Missing latitude", ValidationProblemType.MISSING_LATITUDE);
            isValid = false;
        }
        if (data.getLongitude() == null) {
            data.addValidationError("Missing longitude", ValidationProblemType.MISSING_LONGITUDE);
            isValid = false;
        }
        if (data.getActualSpeedOverground() == null) {
            data.addValidationError("Missing actual speed over ground", ValidationProblemType.MISSING_ACTUAL_SPEED);
            isValid = false;
        } else if (data.getActualSpeedOverground() < 0) {
            data.addValidationError("Negative actual speed over ground", ValidationProblemType.NEGATIVE_ACTUAL_SPEED);
            isValid = false;
        }
        if (data.getProposedSpeedOverground() == null) {
            data.addValidationError("Missing proposed speed over ground", ValidationProblemType.MISSING_PROPOSED_SPEED);
            isValid = false;
        } else if (data.getProposedSpeedOverground() < 0) {
            data.addValidationError("Negative proposed speed over ground", ValidationProblemType.NEGATIVE_PROPOSED_SPEED);
            isValid = false;
        }
        if (isOutlier(data.getActualSpeedOverground(), data.getProposedSpeedOverground())) {
            data.addValidationError("Speed difference is an outlier", ValidationProblemType.OUTLIER);
            isValid = false;
        }

        data.setValidationStatus(isValid ? ValidationStatus.VALID : ValidationStatus.INVALID);
    }

    public boolean isOutlier(Double actualSpeed, Double proposedSpeed) {
        if (actualSpeed == null || proposedSpeed == null) {
            return false;
        }
        double speedDifference = Math.abs(actualSpeed - proposedSpeed);
        return speedDifference > outlierThreshold;
    }
}
