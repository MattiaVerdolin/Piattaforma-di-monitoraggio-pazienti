package ch.supsi.dti.homenet.service.parser;

import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.MeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static ch.supsi.dti.homenet.enums.JsonKeys.*;
import static ch.supsi.dti.homenet.enums.JsonKeys.TIME_OFFSET_SPO2_VALUES;
import static ch.supsi.dti.homenet.enums.SummaryTypes.*;

@Component
public class PulseOxParser extends AbstractParser {

    @Override
    public boolean supports(String summaryType) {
        return summaryType.equals(PULSEOX.getKey());
    }

    @Override
    public void parse(List<Map<String, Object>> data) {
        data.forEach(entry -> {
            String userAccessToken = (String) getValue(USER_ACCESS_TOKEN, entry);
            // get user
            User user = getUserFromToken(userAccessToken);

            //get date of the measurement
            final long startAcquisitionTime = Long.parseLong(getValue(START_TIME_IN_SECONDS, entry).toString()); //Epoch Timestamp: seconds from 1 january 1970 (UTC)
            final int timestampOffset = Integer.parseInt(getValue(START_TIME_OFFSET_IN_SECONDS, entry).toString()); //offset per fuso orario
            Date date = getDate(startAcquisitionTime, timestampOffset);

            //if it's onDemand, isDaily = false because it's in realtime
            final boolean isDaily = !Boolean.parseBoolean(entry.get("onDemand").toString());

            //collect every data in a list from the json collection
            Map<String, Object> spo2Map = (Map<String, Object>) getValue(TIME_OFFSET_SPO2_VALUES, entry);

            if (spo2Map != null && !spo2Map.isEmpty()) {
                List<Double> spo2Values = spo2Map.values().stream()
                        .map(value -> {
                            try {
                                return Double.parseDouble(value.toString());
                            } catch (NumberFormatException e) {
                                logger.warn("Valore SPO2 non valido: " + value);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

                if (!spo2Values.isEmpty()) {
                    double averageSpo2 = Math.round(spo2Values.stream().mapToDouble(Double::doubleValue).average().orElse(0));
                    double minSpo2 = spo2Values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                    double maxSpo2 = spo2Values.stream().mapToDouble(Double::doubleValue).max().orElse(0);

                    storeOrUpdateMeasurement(user, date, minSpo2, MIN_SPO2, isDaily);
                    storeOrUpdateMeasurement(user, date, maxSpo2, MAX_SPO2, isDaily);
                    storeOrUpdateMeasurement(user, date, averageSpo2, AVERAGE_SPO2, isDaily);

                    saveNewSamples(user, spo2Map, startAcquisitionTime, timestampOffset, SPO2);
                } else {
                    logger.warn("Spo2 values erano tutti nulli per user {}", user.getUuid());
                }
            } else {
                logger.warn("spo2Map null o vuota per user {}", user.getUuid());
            }
        });
    }
}
