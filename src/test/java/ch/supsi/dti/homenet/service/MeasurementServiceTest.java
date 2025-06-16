package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.model.Measurement;
import ch.supsi.dti.homenet.model.MeasurementType;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.MeasurementRepository;
import ch.supsi.dti.homenet.repository.MeasurementTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MeasurementServiceTest {
    private MeasurementRepository measurementRepository;
    private MeasurementTypeRepository measurementTypeRepository;
    private MeasurementService measurementService;

    private final User fakeUser = new User();
    private final Date fakeDate = new GregorianCalendar(2024, Calendar.JANUARY, 1).getTime();

    @BeforeEach
    void setUp() {
        measurementRepository = mock(MeasurementRepository.class);
        measurementTypeRepository = mock(MeasurementTypeRepository.class);
        measurementService = new MeasurementService(measurementRepository, measurementTypeRepository);
        fakeUser.setId(1L);
    }

    @Test
    void testGetHeartRateData_withData() {
        MeasurementType avgType = new MeasurementType(); avgType.setName("averageHeartRate");
        MeasurementType minType = new MeasurementType(); minType.setName("minHeartRate");
        MeasurementType maxType = new MeasurementType(); maxType.setName("maxHeartRate");

        when(measurementTypeRepository.findByName(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            MeasurementType type = new MeasurementType();
            type.setName(name);
            return type;
        });


        Measurement m = new Measurement(); m.setValue(75.0); m.setData(fakeDate);
        when(measurementRepository.findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(eq(fakeUser), any(), eq(fakeDate)))
                .thenReturn(List.of(m));

        Map<String, String> result = measurementService.getHeartRateData(fakeUser, fakeDate);

        assertEquals("75", result.get("avg"));
        assertEquals("75", result.get("min"));
        assertEquals("75", result.get("max"));
    }

    @Test
    void testGetMeasurementValue_noType() {
        when(measurementTypeRepository.findByName("heartRate")).thenReturn(null);
        String value = measurementService.getHeartRateData(fakeUser, fakeDate).get("avg");
        assertEquals("---", value);
    }

    @Test
    void testGetSpo2Data_withMeasurements() {
        MeasurementType type = new MeasurementType(); type.setName("averageSpo2");
        when(measurementTypeRepository.findByName(anyString())).thenReturn(type);

        Measurement m = new Measurement(); m.setValue(95.0); m.setData(fakeDate);
        when(measurementRepository.findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(eq(fakeUser), any(), eq(fakeDate)))
                .thenReturn(List.of(m));

        Map<String, String> result = measurementService.getSpo2Data(fakeUser, fakeDate);
        assertEquals("95", result.get("avg"));
        assertEquals("95", result.get("min"));
        assertEquals("95", result.get("max"));
    }

    @Test
    void testGetHeartRateTimeline_withData() {
        MeasurementType type = new MeasurementType(); type.setName("heartRate");
        when(measurementTypeRepository.findByName("heartRate")).thenReturn(type);

        Measurement m = new Measurement(); m.setValue(70.0); m.setData(new GregorianCalendar(2024, Calendar.JANUARY, 1, 10, 15).getTime());
        when(measurementRepository.findAllMeasurementsByPatientAndMeasurementTypeAndDateDay(eq(fakeUser), eq(type), eq(fakeDate)))
                .thenReturn(List.of(m));

        List<Map<String, Object>> timeline = measurementService.getHeartRateTimeline(fakeUser, fakeDate);

        assertEquals(1, timeline.size());
        assertEquals("10:15", timeline.get(0).get("time"));
        assertEquals(70, timeline.get(0).get("value"));
    }

    @Test
    void testGetSpo2Timeline_typeNotFound() {
        when(measurementTypeRepository.findByName("spo2")).thenReturn(null);
        List<Map<String, Object>> result = measurementService.getSpo2Timeline(fakeUser, fakeDate);
        assertTrue(result.isEmpty());
    }
}
