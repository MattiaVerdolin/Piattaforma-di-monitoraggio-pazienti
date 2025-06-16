package ch.supsi.dti.homenet.service.parser;

import ch.supsi.dti.homenet.model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static ch.supsi.dti.homenet.enums.JsonKeys.TIME_OFFSET_STRESS_LEVEL_VALUES;
import static ch.supsi.dti.homenet.enums.JsonKeys.USER_ACCESS_TOKEN;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class StressParserTest {

    private StressParser parser;
    private User mockUser;
    private List<Map<String, Object>> capturedEntries;

    @Before
    public void setUp() {
        mockUser = new User().setUuid("mock-uuid");
        capturedEntries = new ArrayList<>();

        parser = spy(new StressParser() {
            @Override
            protected User getUserFromToken(String token) {
                return mockUser;
            }

            @Override
            protected void saveNewSamples(User user, Map<String, Object> sampleMap, long startTime, int offsetSeconds, ch.supsi.dti.homenet.enums.JsonKeys type) {
                assertEquals("mock-uuid", user.getUuid());
                assertEquals(1743976800L, startTime);
                assertEquals(7200, offsetSeconds);
                assertEquals(ch.supsi.dti.homenet.enums.JsonKeys.STRESS, type);
                capturedEntries.add(sampleMap);
            }
        });
    }

    @Test
    public void testParse_withValidStressData() {
        Map<String, Object> stressData = new HashMap<>();
        stressData.put(USER_ACCESS_TOKEN.getKey(), "mock-token");
        stressData.put("startTimeInSeconds", 1743976800L);
        stressData.put("startTimeOffsetInSeconds", 7200);
        stressData.put(TIME_OFFSET_STRESS_LEVEL_VALUES.getKey(), Map.of(
                "30780", -2,
                "30960", -1,
                "31140", 48,
                "31320", 38
        ));

        parser.parse(List.of(stressData));

        // Verifica che i dati siano stati catturati e contengano valori validi
        assertEquals(1, capturedEntries.size());
        Map<String, Object> saved = capturedEntries.get(0);
        assertEquals(4, saved.size());
        assertEquals(48, saved.get("31140"));
        assertEquals(38, saved.get("31320"));
    }

    @Test
    public void testParse_withNullOrEmptyStressMap() {
        Map<String, Object> entryWithNull = new HashMap<>();
        entryWithNull.put(USER_ACCESS_TOKEN.getKey(), "mock-token");
        entryWithNull.put("startTimeInSeconds", 1743976800L);
        entryWithNull.put("startTimeOffsetInSeconds", 7200);
        entryWithNull.put(TIME_OFFSET_STRESS_LEVEL_VALUES.getKey(), null);

        Map<String, Object> entryWithEmpty = new HashMap<>(entryWithNull);
        entryWithEmpty.put(TIME_OFFSET_STRESS_LEVEL_VALUES.getKey(), new HashMap<>());

        parser.parse(List.of(entryWithNull, entryWithEmpty));

        // Nessun dato dovrebbe essere salvato
        assertEquals(0, capturedEntries.size());
    }

    @Test
    public void testSupports_correctSummaryKey() {
        assertTrue(parser.supports("stressDetails"));
        assertFalse(parser.supports("pulseox"));
    }
}

