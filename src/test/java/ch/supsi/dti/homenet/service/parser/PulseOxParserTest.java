package ch.supsi.dti.homenet.service.parser;

import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static ch.supsi.dti.homenet.enums.JsonKeys.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PulseOxParserTest {

    private PulseOxParser parser;
    private User mockUser;
    private List<String> measurementTypes;
    private Map<String, Object> capturedSamples;

    @Before
    public void setUp() {
        mockUser = new User().setUuid("test-user");
        measurementTypes = new ArrayList<>();
        capturedSamples = new HashMap<>();

        parser = spy(new PulseOxParser() {
            @Override
            protected User getUserFromToken(String token) {
                assertEquals("fake-token", token);
                return mockUser;
            }

            @Override
            protected void storeOrUpdateMeasurement(User user, Date date, Double value, JsonKeys type, boolean isDaily) {
                assertEquals("test-user", user.getUuid());
                assertTrue(value >= 0);
                assertTrue(isDaily);
                measurementTypes.add(type.getKey());
            }

            @Override
            protected void saveNewSamples(User user, Map<String, Object> sampleMap, long startTime, int offsetSeconds, JsonKeys type) {
                assertEquals("test-user", user.getUuid());
                assertEquals(SPO2, type);
                capturedSamples.putAll(sampleMap);
            }
        });
    }

    @Test
    public void testParse_validSpo2Data() {
        Map<String, Object> pulseoxEntry = new HashMap<>();
        pulseoxEntry.put(USER_ACCESS_TOKEN.getKey(), "fake-token");
        pulseoxEntry.put(START_TIME_IN_SECONDS.getKey(), 1743976800L);
        pulseoxEntry.put(START_TIME_OFFSET_IN_SECONDS.getKey(), 7200);
        pulseoxEntry.put("onDemand", false);
        pulseoxEntry.put(TIME_OFFSET_SPO2_VALUES.getKey(), Map.of(
                "31860", 96,
                "31920", 95,
                "31980", 97
        ));

        parser.parse(List.of(pulseoxEntry));

        // Verifica le misurazioni
        assertTrue(measurementTypes.containsAll(List.of(
                MIN_SPO2.getKey(),
                MAX_SPO2.getKey(),
                AVERAGE_SPO2.getKey()
        )));
        assertEquals(3, measurementTypes.size());

        // Verifica salvataggio singoli sample
        assertEquals(3, capturedSamples.size());
        assertTrue(capturedSamples.containsKey("31860"));
    }

    @Test
    public void testParse_emptyMap_skips() {
        Map<String, Object> entry = new HashMap<>();
        entry.put(USER_ACCESS_TOKEN.getKey(), "fake-token");
        entry.put(START_TIME_IN_SECONDS.getKey(), 1743976800L);
        entry.put(START_TIME_OFFSET_IN_SECONDS.getKey(), 7200);
        entry.put("onDemand", false);
        entry.put(TIME_OFFSET_SPO2_VALUES.getKey(), Collections.emptyMap());

        parser.parse(List.of(entry));

        assertTrue(measurementTypes.isEmpty());
        assertTrue(capturedSamples.isEmpty());
    }

    @Test
    public void testParse_nullSpo2Map_skips() {
        Map<String, Object> entry = new HashMap<>();
        entry.put(USER_ACCESS_TOKEN.getKey(), "fake-token");
        entry.put(START_TIME_IN_SECONDS.getKey(), 1743976800L);
        entry.put(START_TIME_OFFSET_IN_SECONDS.getKey(), 7200);
        entry.put("onDemand", false);
        entry.put(TIME_OFFSET_SPO2_VALUES.getKey(), null);

        parser.parse(List.of(entry));

        assertTrue(measurementTypes.isEmpty());
        assertTrue(capturedSamples.isEmpty());
    }
}
