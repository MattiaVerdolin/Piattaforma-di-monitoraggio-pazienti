package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.AlertMargin;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.AlertMarginRepository;
import ch.supsi.dti.homenet.repository.MeasurementTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlertMarginService {

    private final AlertMarginRepository alertMarginRepository;
    private final MeasurementTypeRepository measurementTypeRepository;

    public AlertMarginService(AlertMarginRepository alertMarginRepository, MeasurementTypeRepository measurementTypeRepository) {
        this.alertMarginRepository = alertMarginRepository;
        this.measurementTypeRepository = measurementTypeRepository;
    }

    public void saveOrUpdateMargin(User patient, String measurementName, Double min, Double max) {
        if (min == null && max == null) return;

        MeasurementType type = createOrFindMeasurementType(measurementName);

        AlertMargin margin = alertMarginRepository.findByPatientAndMeasurementType(patient, type)
                .orElseGet(() -> {
                    AlertMargin newMargin = new AlertMargin();
                    newMargin.setPatient(patient);
                    newMargin.setMeasurementType(type);
                    return newMargin;
                });

        margin.setMin(min);
        margin.setMax(max);

        alertMarginRepository.save(margin);
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

    public Map<String, AlertMargin> getMarginsByPatient(User patient) {
        return alertMarginRepository.findAllByPatient(patient).stream()
                .collect(Collectors.toMap(m -> m.getMeasurementType().getName(), m -> m));
    }

    @Transactional
    public void deleteByPatient(User patient) {
        alertMarginRepository.deleteByPatient(patient);
    }
}


