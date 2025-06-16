package ch.supsi.dti.homenet.service.parser;

import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.Measurement;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.MeasurementRepository;
import ch.supsi.dti.homenet.repository.MeasurementTypeRepository;
import ch.supsi.dti.homenet.repository.UserRepository;
import ch.supsi.dti.homenet.service.GarminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractParser implements SummaryParser {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MeasurementTypeRepository measurementTypeRepository;
    @Autowired
    private MeasurementRepository measurementRepository;

    protected static final Logger logger = LoggerFactory.getLogger(GarminService.class);

    protected User getUserFromToken(String token) {
        return userRepository.findOneByToken(token)
                .orElseThrow(() -> new RuntimeException("Utente non trovato per token: " + token));
    }

    private MeasurementType createOrFindMeasurementType(String type) {
        MeasurementType mType = measurementTypeRepository.findByName(type);
        if (mType == null) {
            mType = new MeasurementType();
            mType.setName(type);
            mType = measurementTypeRepository.save(mType);
        }
        return mType;
    }

    private Optional<Measurement> findExistingMeasurement(User user, MeasurementType type, Date date) {
        return measurementRepository.findByPatientAndMeasurementTypeAndData(user, type, date);
    }

    protected void storeOrUpdateMeasurement(User user, Date date, Double value, JsonKeys measurementType, boolean isDaily) {
        MeasurementType type = createOrFindMeasurementType(measurementType.getKey()); // identfico il tipo di misurazione dall'enum passato come param
        Optional<Measurement> existing = findExistingMeasurement(user, type, date); // controllo se esiste quella specifica misurazione con check su user, tipo misurazione e data

        Measurement measurement = existing.orElseGet(() -> { // se esiste, utilizzo quella, altrimenti la creo nuova (orElseGet)
            Measurement m = new Measurement();
            m.setPatient(user);
            m.setMeasurementType(type);
            m.setData(date);
            m.setIsDaily(isDaily);
            return m;
        });

        measurement.setValue((double) Math.round(value)); // setto o sovrascrivo il valore
        measurementRepository.save(measurement); // salvo
    }

    protected void saveNewSamples(User user, Map<String, Object> sampleMap, long startTime, int offsetSeconds, JsonKeys type) {
        ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offsetSeconds);

        Optional<LocalDateTime> lastSavedDateTimeOpt = measurementRepository
                .findTopByPatientAndMeasurementTypeOrderByDataDesc(user, createOrFindMeasurementType(type.getKey()))
                .map(measurement -> measurement.getData().toInstant().atOffset(zoneOffset).toLocalDateTime());

        for (Map.Entry<String, Object> sample : sampleMap.entrySet()) {
            long offset = Long.parseLong(sample.getKey());
            long exactTimestamp = startTime + offset;

            LocalDateTime sampleTime = Instant.ofEpochSecond(exactTimestamp).atOffset(zoneOffset).toLocalDateTime();

            // salva solo se il campione è più recente
            if (lastSavedDateTimeOpt.isEmpty() || sampleTime.isAfter(lastSavedDateTimeOpt.get())) {
                Date fullDate = Date.from(sampleTime.toInstant(zoneOffset));
                Double value = ((double) Math.round(Double.parseDouble(sample.getValue().toString())));

                storeOrUpdateMeasurement(user, fullDate, value, type, false);
            }
        }
    }

    protected Date getDate(long startAcquisitionTime, int timestampOffset) {
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(timestampOffset);
        return Date.from(Instant.ofEpochSecond(startAcquisitionTime).atOffset(offset).toInstant());
    }

    protected Object getValue(JsonKeys key, Map<String, Object> entry) {
        Object value = entry.get(key.getKey());
        if (value == null) {
            logger.warn("Key [{}] not found or null in entry: {}", key.getKey(), entry);
        }
        return value;
    }

}
