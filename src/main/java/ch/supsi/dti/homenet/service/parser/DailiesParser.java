package ch.supsi.dti.homenet.service.parser;

import ch.supsi.dti.homenet.enums.SummaryTypes;
import ch.supsi.dti.homenet.model.Measurement;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ch.supsi.dti.homenet.enums.JsonKeys.*;
import static ch.supsi.dti.homenet.enums.SummaryTypes.*;


@Component
public class DailiesParser extends AbstractParser {

    @Override
    public boolean supports(String summaryType) {
        return summaryType.equals(DAILIES.getKey());
    }

    @Override
    public void parse(List<Map<String, Object>> data) {
        data.forEach(entry -> {
            String userAccessToken = (String) getValue(USER_ACCESS_TOKEN, entry);

            final boolean isDaily = true;
            User user = getUserFromToken(userAccessToken);

            //get date of the measurement
            final long startAcquisitionTime = Long.parseLong(getValue(START_TIME_IN_SECONDS, entry).toString()); //Epoch Timestamp: seconds from 1 january 1970 (UTC)
            final int timestampOffset = Integer.parseInt(getValue(START_TIME_OFFSET_IN_SECONDS, entry).toString()); //offset per fuso orario
            Date date = getDate(startAcquisitionTime, timestampOffset);

            //steps
            final Double steps = Double.valueOf(getValue(STEPS, entry).toString());
            storeOrUpdateMeasurement(user, date, steps, STEPS, isDaily);

            //min heart rate
            final Double minHeartRate = Double.valueOf(getValue(MIN_HEART_RATE, entry).toString());
            storeOrUpdateMeasurement(user, date, minHeartRate, MIN_HEART_RATE, isDaily);

            //max heart rate
            final Double maxHeartRate = Double.valueOf(getValue(MAX_HEART_RATE, entry).toString());
            storeOrUpdateMeasurement(user, date, maxHeartRate, MAX_HEART_RATE, isDaily);

            //average heart rate
            final Double averageHeartRate = Double.valueOf(getValue(AVERAGE_HEART_RATE, entry).toString());
            storeOrUpdateMeasurement(user, date, averageHeartRate, AVERAGE_HEART_RATE, isDaily);

            final Double maxStressLevel = Double.valueOf(getValue(MAX_STRESS_LEVEL, entry).toString());
            storeOrUpdateMeasurement(user, date, maxStressLevel, MAX_STRESS_LEVEL, isDaily);

            final Double averageStressLevel = Double.valueOf(getValue(AVERAGE_STRESS_LEVEL, entry).toString());
            storeOrUpdateMeasurement(user, date, averageStressLevel, AVERAGE_STRESS_LEVEL, isDaily);

            //single heart rate measurements
            final Map<String, Object> heartRateMap = (Map<String, Object>) getValue(TIME_OFFSET_HEART_RATE_SAMPLES, entry);

            //salva le singole misurazioni del battito cardiaco
            if (heartRateMap != null && !heartRateMap.isEmpty()) {
                saveNewSamples(user, heartRateMap, startAcquisitionTime, timestampOffset, HEART_RATE);
            }

        });
    }
}
