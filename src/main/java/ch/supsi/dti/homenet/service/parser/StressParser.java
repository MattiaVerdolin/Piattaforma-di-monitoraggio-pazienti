package ch.supsi.dti.homenet.service.parser;

import ch.supsi.dti.homenet.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ch.supsi.dti.homenet.enums.SummaryTypes.*;
import static ch.supsi.dti.homenet.enums.JsonKeys.*;

@Component
public class StressParser extends AbstractParser {
    @Override
    public boolean supports(String summaryType) {
        return summaryType.equals(STRESS_DETAILS.getKey());
    }

    @Override
    public void parse(List<Map<String, Object>> data) {
        data.forEach(entry -> {
            String userAccessToken = (String) getValue(USER_ACCESS_TOKEN, entry);
            User user = getUserFromToken(userAccessToken);

            final long startAcquisitionTime = Long.parseLong(getValue(START_TIME_IN_SECONDS, entry).toString());
            final int timestampOffset = Integer.parseInt(getValue(START_TIME_OFFSET_IN_SECONDS, entry).toString());

            Map<String, Object> stressMap = (Map<String, Object>) getValue(TIME_OFFSET_STRESS_LEVEL_VALUES, entry);

            if (stressMap != null && !stressMap.isEmpty()) {
                List<Double> stressValues = stressMap.values().stream()
                        .map(value -> {
                            try {
                                return Double.parseDouble(value.toString());
                            } catch (NumberFormatException e) {
                                logger.warn("Valore stress non valido: {}", value);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

                if (!stressValues.isEmpty()) {
                    saveNewSamples(user, stressMap, startAcquisitionTime, timestampOffset, STRESS);
                } else {
                    logger.warn("Tutti i valori stress erano nulli o non validi per user {}", user.getUuid());
                }
            } else {
                logger.warn("stressMap null o vuota per user {}", user.getUuid());
            }
        });
    }
}
