package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.exception.GarminConnectException;
import ch.supsi.dti.homenet.model.User;
import ch.supsi.dti.homenet.repository.DiseaseCategoryRepository;
import ch.supsi.dti.homenet.repository.UserRepository;
import ch.supsi.dti.homenet.service.parser.SummaryParser;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GarminServiceTest {

    private GarminService garminService;
    private UserRepository userRepository;
    private DiseaseCategoryRepository diseaseCategoryRepository;
    private List<SummaryParser> summaryParsers;

    @Before
    public void setUp() {
        userRepository = mock(UserRepository.class);
        diseaseCategoryRepository = mock(DiseaseCategoryRepository.class);
        summaryParsers = new ArrayList<>();
        garminService = new GarminService(summaryParsers, userRepository, diseaseCategoryRepository);

        // inject values for test
        ReflectionTestUtils.setField(garminService, "consumerkey", "key");
        ReflectionTestUtils.setField(garminService, "consumerSecret", "secret");
        ReflectionTestUtils.setField(garminService, "garminAccessTokenUrl", "http://mock/access");
        ReflectionTestUtils.setField(garminService, "garminCallbackUrl", "http://mock/callback/$$UUID$$");
        ReflectionTestUtils.setField(garminService, "garminOAuthAuthenticationUrl", "http://mock/auth");
        ReflectionTestUtils.setField(garminService, "garminOauthTemporaryTokenUrlRequest", "http://mock/temp");

    }

    @Test
    public void testGetUser_found() throws GarminConnectException {
        User user = new User().setUuid("test-uuid");
        when(userRepository.findOneByUuid("test-uuid")).thenReturn(Optional.of(user));
        User result = garminService.getUser("test-uuid");
        assertEquals("test-uuid", result.getUuid());
    }

    @Test(expected = GarminConnectException.class)
    public void testGetUser_userNotFound() throws GarminConnectException {
        when(userRepository.findOneByUuid("missing")).thenReturn(Optional.empty());
        garminService.getUser("missing");
    }

    @Test
    public void testVerifyToken_true_withReflection() throws Exception {
        Method method = GarminService.class.getDeclaredMethod("verifyToken", String.class);
        method.setAccessible(true);

        when(userRepository.findOneByToken("token123")).thenReturn(Optional.of(new User()));

        boolean result = (boolean) method.invoke(garminService, "token123");
        assertTrue(result);
    }

    @Test
    public void testVerifyToken_false_withReflection() throws Exception {
        Method method = GarminService.class.getDeclaredMethod("verifyToken", String.class);
        method.setAccessible(true);

        when(userRepository.findOneByToken("token123")).thenReturn(Optional.empty());

        boolean result = (boolean) method.invoke(garminService, "token123");
        assertFalse(result);
    }

    @Test
    public void testGenerateAuthenticationUrl_alreadyAssociated() throws Exception {
        User user = new User().setToken("a").setTokenSecret("b");
        when(userRepository.findOneByUuid("uuid")).thenReturn(Optional.of(user));
        String result = garminService.generateAuthenticationUrl("uuid");
        assertEquals("/alreadyassociated.html", result);
    }

    @Test
    public void testHandlePayload_parsingPulseOxJson() {
        // SummaryParser mock che accetta "pulseox" e verifica che venga chiamato
        SummaryParser parser = mock(SummaryParser.class);
        when(parser.supports("pulseox")).thenReturn(true);
        summaryParsers.add(parser);

        // User mock
        User user = new User().setUuid("user-id").setToken("fake-token");
        when(userRepository.findOneByToken("fake-token")).thenReturn(Optional.of(user));

        // JSON di esempio
        String payload = "{\n" +
                "  \"pulseox\": [\n" +
                "    {\n" +
                "      \"userAccessToken\": \"fake-token\",\n" +
                "      \"userId\": \"user-id\",\n" +
                "      \"summaryId\": \"summary-id\",\n" +
                "      \"calendarDate\": \"2025-04-06\",\n" +
                "      \"startTimeInSeconds\": 1743890400,\n" +
                "      \"startTimeOffsetInSeconds\": 7200,\n" +
                "      \"timeOffsetSpo2Values\": {\n" +
                "        \"31200\": 95,\n" +
                "        \"31320\": 95\n" +
                "      },\n" +
                "      \"onDemand\": false\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        garminService.handlePayload(payload);

        verify(parser, times(1)).parse(anyList());
    }

    @Test
    public void testHandleDeregistration_savesUpdatedUser() {
        summaryParsers.clear(); // Importante: nessun parser deve gestirlo

        User user = new User().setUuid("token-123");
        Map<String, Object> deregData = new HashMap<>();
        deregData.put("userAccessToken", "token-123");
        List<Map<String, Object>> data = List.of(deregData);

        when(userRepository.findOneByToken("token-123")).thenReturn(Optional.of(user));
        when(userRepository.findOneByUuid("token-123")).thenReturn(Optional.of(user));

        // JSON con deregistration
        String payload = "{ \"deregistration\": [ { \"userAccessToken\": \"token-123\" } ] }";

        garminService.handlePayload(payload);

        // Verifica che il salvataggio sia stato fatto
        verify(userRepository).save(any(User.class));
    }
}
