package ch.supsi.dti.homenet.service.parser;

import ch.supsi.dti.homenet.enums.JsonKeys;
import ch.supsi.dti.homenet.model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static ch.supsi.dti.homenet.enums.JsonKeys.*;
import static ch.supsi.dti.homenet.enums.JsonKeys.TIME_OFFSET_HEART_RATE_SAMPLES;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DailiesParserTest {

    private DailiesParser parser;
    private User mockUser;
    private List<String> calledMeasurementTypes;
    private Map<String, Object> capturedSamples;

    @Before
    public void setUp() {
        mockUser = new User().setUuid("fake-uuid");
        calledMeasurementTypes = new ArrayList<>();
        capturedSamples = new HashMap<>();

        parser = spy(new DailiesParser() {
            @Override
            protected User getUserFromToken(String token) {
                return mockUser;
            }

            @Override
            protected void storeOrUpdateMeasurement(User user, Date date, Double value, JsonKeys type, boolean isDaily) {
                assertEquals("fake-uuid", user.getUuid());
                assertNotNull(date);
                assertNotNull(value);
                assertTrue(isDaily);
                calledMeasurementTypes.add(type.getKey());
            }

            @Override
            protected void saveNewSamples(User user, Map<String, Object> sampleMap, long startTime, int offsetSeconds, JsonKeys type) {
                assertEquals("fake-uuid", user.getUuid());
                assertEquals(1743890400L, startTime);
                assertEquals(7200, offsetSeconds);
                assertEquals(JsonKeys.HEART_RATE, type);
                capturedSamples.putAll(sampleMap);
            }
        });
    }

    @Test
    public void testParse_storesExpectedMeasurements() {
        Map<String, Object> entry = new HashMap<>();
        entry.put(USER_ACCESS_TOKEN.getKey(), "fake-uuid");
        entry.put(START_TIME_IN_SECONDS.getKey(), 1743890400L);
        entry.put(START_TIME_OFFSET_IN_SECONDS.getKey(), 7200);
        entry.put(STEPS.getKey(), 4320);
        entry.put(MIN_HEART_RATE.getKey(), 55);
        entry.put(MAX_HEART_RATE.getKey(), 130);
        entry.put(AVERAGE_HEART_RATE.getKey(), 82);
        entry.put(MAX_STRESS_LEVEL.getKey(), 38);
        entry.put(AVERAGE_STRESS_LEVEL.getKey(), 21);
        entry.put(TIME_OFFSET_HEART_RATE_SAMPLES.getKey(), Map.of("29700", 75, "29730", 78));

        parser.parse(List.of(entry));

        List<String> expectedKeys = List.of(
                STEPS.getKey(), MIN_HEART_RATE.getKey(), MAX_HEART_RATE.getKey(),
                AVERAGE_HEART_RATE.getKey(), MAX_STRESS_LEVEL.getKey(), AVERAGE_STRESS_LEVEL.getKey()
        );

        assertTrue(calledMeasurementTypes.containsAll(expectedKeys));
        assertEquals(6, calledMeasurementTypes.size());
        assertEquals(2, capturedSamples.size());
        assertTrue(capturedSamples.containsKey("29700"));
        assertTrue(capturedSamples.containsKey("29730"));
    }
}
