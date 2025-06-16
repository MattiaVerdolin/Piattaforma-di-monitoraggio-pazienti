package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.controller.ConnectController;
import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.Measurement;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.MeasurementRepository;
import ch.supsi.dti.homenet.repository.MeasurementTypeRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MeasurementService {
    private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);
    private final MeasurementRepository measurementRepository;
    private final MeasurementTypeRepository measurementTypeRepository;

    @Autowired
    public MeasurementService(MeasurementRepository measurementRepository, MeasurementTypeRepository measurementTypeRepository) {
        this.measurementRepository = measurementRepository;
        this.measurementTypeRepository = measurementTypeRepository;
    }

    public Optional<Measurement> findByPatientAndMeasurementTypeAndData(User patient, MeasurementType measurementType, Date data) {
        return measurementRepository.findByPatientAndMeasurementTypeAndData(patient, measurementType, data);
    }


    public Map<String, String> getHeartRateData(User patient, Date date) {
        Map<String, String> result = new HashMap<>();
        JsonKeys avgHeartRate = JsonKeys.AVERAGE_HEART_RATE;
        JsonKeys minHeartRate = JsonKeys.MIN_HEART_RATE;
        JsonKeys maxHeartRate = JsonKeys.MAX_HEART_RATE;

        // Fetch the measurement values for each type
        result.put("avg", getMeasurementValue(patient, date, avgHeartRate.getKey()));
        result.put("min", getMeasurementValue(patient, date, minHeartRate.getKey()));
        result.put("max", getMeasurementValue(patient, date, maxHeartRate.getKey()));

        return result;

    }

    public Map<String, String> getSpo2Data(User patient, Date date) {
        return getMeasurementData(patient, date, "Spo2");
    }

    public Map<String, String> getStressData(User patient, Date date) {
        return getMeasurementData(patient, date, "StressLevel");
    }

    public Map<String, String> getStepsData(User patient, Date date) {
        Map<String, String> result = new HashMap<>();

        MeasurementType type = measurementTypeRepository.findByName(JsonKeys.STEPS.getKey());
        if (type == null) {
            logger.warn("MeasurementType not found: steps");
            result.put("total", "---");
            return result;
        }

        List<Measurement> measurements = measurementRepository.findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(patient, type, date);

        if (measurements.isEmpty()) {
            logger.warn("No steps measurements found for patient: " + patient.getId() + ", date: " + date);
            result.put("total", "---");
        } else {
            // Find the maximum value
            int maxSteps = measurements.stream()
                    .mapToInt(m -> m.getValue().intValue())
                    .max()
                    .orElse(0);

            result.put("total", String.valueOf(maxSteps));
        }

        return result;
    }


    private Map<String, String> getMeasurementData(User patient, Date date, String measurementTypeBaseName) {
        Map<String, String> result = new HashMap<>();

        result.put("max", getMeasurementValue(patient, date, "max" + measurementTypeBaseName));
        result.put("avg", getMeasurementValue(patient, date, "average" + measurementTypeBaseName));
        result.put("min", getMeasurementValue(patient, date, "min" + measurementTypeBaseName));

        return result;
    }

    public List<Map<String, Object>> getHeartRateTimeline(User patient, Date date) {
        MeasurementType type = measurementTypeRepository.findByName("heartRate");
        if (type == null) {
            logger.warn("MeasurementType not found: heartRate");
            return Collections.emptyList();
        }
        List<Measurement> measurements = measurementRepository.findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(patient, type, date);
        List<Map<String, Object>> timeline = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (Measurement m : measurements) {
            Map<String, Object> point = new HashMap<>();
            point.put("time", sdf.format(m.getData())); // estrae HH:mm dal campo Date
            point.put("value", m.getValue().intValue());
            timeline.add(point);
        }
        return timeline;
    }

    public List<Map<String, Object>> getSpo2Timeline(User patient, Date date) {
        MeasurementType type = measurementTypeRepository.findByName("spo2");
        if (type == null) {
            logger.warn("MeasurementType not found: spo2");
            return Collections.emptyList();
        }
        List<Measurement> measurements = measurementRepository.findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(patient, type, date);
        List<Map<String, Object>> timeline = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (Measurement m : measurements) {
            Map<String, Object> point = new HashMap<>();
            point.put("time", sdf.format(m.getData()));
            point.put("value", m.getValue().intValue());
            timeline.add(point);
        }
        return timeline;
    }


    private String getMeasurementValue(User patient, Date date, String measurementTypeName) {
        MeasurementType type = measurementTypeRepository.findByName(measurementTypeName);
        if (type == null) {
            logger.warn("MeasurementType not found: " + measurementTypeName);
            return "---";
        }

        List<Measurement> measurements = measurementRepository.findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(patient, type, date);
        if (measurements.isEmpty()) {
            logger.warn("No measurements found for: " + measurementTypeName + ", patient: " + patient.getId() + ", date: " + date);
            return "---";
        }

        Measurement latest = measurements.getFirst(); // prende il più recente
        logger.info("Value found for: " + measurementTypeName + ", patient: " + patient.getId() + ", date: " + date);

        return String.valueOf(latest.getValue().intValue());
    }
}
