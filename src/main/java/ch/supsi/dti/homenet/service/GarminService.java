package ch.supsi.dti.homenet.service;

import ch.supsi.dti.homenet.enums.GarminConnectError;
import ch.supsi.dti.homenet.enums.SummaryTypes;
import ch.supsi.dti.homenet.exception.GarminConnectException;
import ch.supsi.dti.homenet.model.*;
import ch.supsi.dti.homenet.repository.*;
import ch.supsi.dti.homenet.service.parser.SummaryParser;
import com.google.api.client.auth.oauth.*;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static ch.supsi.dti.homenet.enums.JsonKeys.USER_ACCESS_TOKEN;
import static ch.supsi.dti.homenet.enums.SummaryTypes.*;

@Service
public class GarminService {

    private static final Logger logger = LoggerFactory.getLogger(GarminService.class);
    private final UserRepository userRepository;
    private final DiseaseCategoryRepository diseaseCategoryRepository;

    // viene popolata automaticamente da Spring cercando e trovando tutti i beam che implementano l'interfaccia SummaryParser
    private final List<SummaryParser> summaryParsers;

    //value written in application.properties
    @Value("${garmin.consumer.secret}")
    private String consumerSecret;

    @Value("${garmin.consumer.key}")
    private String consumerkey;

    @Value("${garmin.oauth.temporarytokenurlrequest}")
    private String garminOauthTemporaryTokenUrlRequest;

    @Value("${garmin.oauth.authenticationurl}")
    private String garminOAuthAuthenticationUrl;

    @Value("${garmin.oauth.callbackurl}")
    private String garminCallbackUrl;

    @Value("${garmin.oauth.accesstokenurl}")
    private String garminAccessTokenUrl;

    @Autowired
    public GarminService(List<SummaryParser> summaryParsers, UserRepository userRepository, DiseaseCategoryRepository diseaseCategoryRepository) {
        this.summaryParsers = summaryParsers;
        this.userRepository = userRepository;
        this.diseaseCategoryRepository = diseaseCategoryRepository;
    }

    public User getUser(String uuid) throws GarminConnectException {
        User user = this.userRepository.findOneByUuid(uuid.toLowerCase()).orElse(null);
        if (user == null)
            throw new GarminConnectException(GarminConnectError.UUID_NOT_FOUND);
        return user;
    }

    public String generateAuthenticationUrl(String uuid) throws IOException, GarminConnectException {
        logger.info("Garmin key: " + this.consumerkey);
        logger.info("Garmin secret: " + this.consumerSecret);
        Optional<User> myUser = this.userRepository.findOneByUuid(uuid.toLowerCase());

        //user is already registered and properly configured with garminConnect
        if(myUser.isPresent() && myUser.get().getToken() != null && myUser.get().getTokenSecret() != null){
            return "/alreadyassociated.html";
        }

        /* First step: get temporary request token and token secret */
        User user = generateUnauthorizedTokenRequest(uuid.toLowerCase());

        /* Second step: user authorization of request token
         *  User enters Garmin login credentials
         *  Then, the Request Token is authorized and ready to be exchanged for an Access Token
         */
        OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(this.garminOAuthAuthenticationUrl);
        accessTempToken.temporaryToken = user.getTemporaryToken();
        /* set callback, which calls the endpoint "connectReceive" that redirects to confirm.html (new voucher/uuid with new GarminConnect) or tokenAlreadyAssociated.html (new voucher with already associated garminAccount) */
        accessTempToken.set("oauth_callback", this.garminCallbackUrl.replace("$$UUID$$", uuid.toLowerCase()));

        return accessTempToken.build();
    }

    /**
     * initial user garmin authentication and creation
     */
    private User generateUnauthorizedTokenRequest(String uuid) throws IOException {
        // Create an instance of OAuthGetTemporaryToken with the temporary token URL
        OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(this.garminOauthTemporaryTokenUrlRequest);

        // Create an instance of OAuthHmacSigner for signing the request
        OAuthHmacSigner signer = new OAuthHmacSigner();

        // Set the client shared secret for the signer
        signer.clientSharedSecret = this.consumerSecret;

        // Set the consumer key for the temporary token request
        getTemporaryToken.consumerKey = this.consumerkey;

        // Set the signer for the temporary token request
        getTemporaryToken.signer = signer;

        // Set the transport mechanism
        getTemporaryToken.transport = new NetHttpTransport();

        // Execute the temporary token request and store the response
        OAuthCredentialsResponse temporaryTokenResponse = getTemporaryToken.execute();
        logger.info("TemporaryToken: " + temporaryTokenResponse.token + " temporary secret: " + temporaryTokenResponse.tokenSecret);

        DiseaseCategory defaultCategory = diseaseCategoryRepository.findByName("default")
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));
        if (defaultCategory == null) {
            defaultCategory = new DiseaseCategory();
            defaultCategory.setName("default");
            defaultCategory = diseaseCategoryRepository.save(defaultCategory);
        }

        User user = this.userRepository.findOneByUuid(uuid.toLowerCase())
                .orElse(new User().setUuid(uuid.toLowerCase()));

        user.setTemporaryToken(temporaryTokenResponse.token)
                .setTemporaryTokenSecret(temporaryTokenResponse.tokenSecret)
                .setToken(null)
                .setTokenSecret(null)
                .setVerifier(null)
                .setDiseaseCategory(defaultCategory);

        return this.userRepository.save(user);
    }

    /**
     * check the user model to see if a user with "token" is already present in the db if yes, updates the user
     * with new data about token and tokenSecret, otherwise it deletes the user that is attempting to register with
     * an already validated garminConnect account.
     */
    public void requestAccessToken(String oAuthVerifier, String uuid) throws GarminConnectException {
        User user = this.getUser(uuid);
        logger.debug("requestAccessToken - User = " + user.getUuid());
        OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(this.garminAccessTokenUrl);
        OAuthHmacSigner psigner = new OAuthHmacSigner();
        psigner.clientSharedSecret = this.consumerSecret;
        psigner.tokenSharedSecret = user.getTemporaryTokenSecret();
        getAccessToken.signer = psigner;
        getAccessToken.temporaryToken = user.getTemporaryToken();
        getAccessToken.transport = new NetHttpTransport();
        getAccessToken.verifier = oAuthVerifier.trim();
        getAccessToken.consumerKey = this.consumerkey.trim();
        try {
            OAuthCredentialsResponse accessTokenResponse = getAccessToken.execute();
            //todo:m consider that the repeat the procedure only to redefine the permissions
            //if (this.userRepository.findOneByToken(accessTokenResponse.token).isEmpty()) {
            Optional<User> existingUser = this.userRepository.findOneByToken(accessTokenResponse.token);
            if (existingUser.isEmpty()) {
                logger.info("Setting token: "+accessTokenResponse.token+ " - secret: " + accessTokenResponse.tokenSecret);
                this.userRepository.save(user
                        .setToken(accessTokenResponse.token)
                        .setTokenSecret(accessTokenResponse.tokenSecret)
                        .setVerifier(oAuthVerifier)
                        .setTemporaryToken(null)
                        .setTemporaryTokenSecret(null));
                this.updatePermissionsForUser(user);
            } else {
                //delete the unauth user that cant complete the procedure because it is trying to access the app with an already signed-in garmin account
                this.userRepository.delete(user);
                throw new GarminConnectException(GarminConnectError.GARMIN_TOKEN_ALREADY_ASSOCIATED);
            }
        } catch (HttpResponseException e) {
            throw new GarminConnectException(GarminConnectError.UUID_AUTHORIZATION_NOT_GRANTED);
        } catch (IOException e) {
            throw new GarminConnectException(GarminConnectError.GENERIC_ERROR, e);
        }
    }

    public HttpResponse makeRequest(String uuid, String url) throws GarminConnectException {
        User user = this.getUser(uuid);
        try {
            OAuthHmacSigner signer = new OAuthHmacSigner();
            OAuthParameters oauthParameters = new OAuthParameters();
            signer.clientSharedSecret = this.consumerSecret;
            signer.tokenSharedSecret = user.getTokenSecret();
            oauthParameters.signer = signer;
            oauthParameters.consumerKey = this.consumerkey;
            oauthParameters.token = user.getToken();
            oauthParameters.verifier = user.getVerifier();

            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(oauthParameters);
            GenericUrl genericUrl = new GenericUrl(url);
            return requestFactory.buildGetRequest(genericUrl).execute();
        } catch (IOException e) {
            throw new GarminConnectException(GarminConnectError.GENERIC_ERROR, e);
        }
    }

    private void updatePermissionsForUser(User user) throws GarminConnectException {
        try {
            String response = new String(this.makeRequest(user.getUuid(), "https://apis.garmin.com/userPermissions").getContent().readAllBytes());
            user = this.userRepository.save(user.setActivityExportPermission(response.contains("ACTIVITY_EXPORT")).setHealthExportPermission(response.contains("HEALTH_EXPORT")));
            logger.info("User " + user.getUuid() + " has granted activity=" + user.getActivityExportPermission() + ", health=" + user.getHealthExportPermission());
        } catch (IOException e) {
            throw new GarminConnectException(GarminConnectError.GENERIC_ERROR, e);
        }
    }

    private static List<Map<String, Object>> transform(JSONArray array) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Map<String, Object> map = new HashMap<>();

            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = obj.get(key);

                if (value instanceof JSONObject jsonObject) {
                    Map<String, Object> innerMap = new HashMap<>();
                    Iterator<String> innerKeys = jsonObject.keys();
                    while (innerKeys.hasNext()) {
                        String innerKey = innerKeys.next();
                        innerMap.put(innerKey, jsonObject.get(innerKey));
                    }
                    map.put(key, innerMap);
                } else if (value instanceof JSONArray jsonArray) {
                    List<Object> innerList = new ArrayList<>();
                    for (int j = 0; j < jsonArray.length(); j++) {
                        innerList.add(jsonArray.get(j));
                    }
                    map.put(key, innerList);
                } else {
                    map.put(key, value);
                }
            }

            list.add(map);
        }
        return list;
    }

    @Async
    public void handlePayload(String payload) {
        logger.info(payload);
        JSONObject object = new JSONObject(payload);

        for (SummaryTypes summary : SummaryTypes.values()) {
            String summaryKey = summary.getKey();

            if(object.has(summaryKey)) {
                List<Map<String,Object>> listOfJSON = transform(object.getJSONArray(summaryKey));

                // filtro per solo gli utenti registrati
                List<Map<String, Object>> registeredUsersData = listOfJSON.stream()
                        .filter(entry -> {
                            String token = String.valueOf(entry.get(USER_ACCESS_TOKEN.getKey()));
                            boolean isRegistered = verifyToken(token);
                            if (!isRegistered) {
                                logger.warn("Token non registrato: " + token);
                            }
                            return isRegistered;
                        })
                        .toList();

                // se il summary type è di tipo DEREGISTRATION, invoca il metodo e continua a ciclare gli altri summaries
                if(summary == DEREGISTRATION){
                    this.handleDeregistration(registeredUsersData);
                    continue;
                }

                // stream per trovare il parser per il summary attuale
                summaryParsers.stream()
                        .filter(p -> p.supports(summaryKey))
                        .findFirst()
                        .ifPresentOrElse(
                                parser -> parser.parse(registeredUsersData),
                                () -> logger.warn("Nessun parser trovato per: " + summaryKey)
                        );
            }
        }
    }

    private boolean verifyToken(String token) {
        return userRepository.findOneByToken(token).isPresent();
    }

    private void handleDeregistration(List<Map<String, Object>> permissionChangeData) {
        for (Map<String, Object> entry : permissionChangeData) {
            Optional<User> user = this.userRepository.findOneByUuid(entry.get("userAccessToken").toString());
            if (user.isPresent()) {
                logger.info("Deregistration for user " + user.get().getUuid());
                this.userRepository.save(user.get()
                        .setHealthExportPermission(false)
                        .setActivityExportPermission(false)
                        .setTemporaryToken(null)
                        .setTemporaryTokenSecret(null)
                        .setVerifier(null)
                );
            }
        }
    }
}

