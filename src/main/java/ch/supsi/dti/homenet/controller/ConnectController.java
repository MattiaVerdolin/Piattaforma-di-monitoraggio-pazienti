package ch.supsi.dti.homenet.controller;

import ch.supsi.dti.homenet.enums.GarminConnectError;
import ch.supsi.dti.homenet.exception.GarminConnectException;
import ch.supsi.dti.homenet.service.GarminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * it handles incoming data from garmin api
 */

@Controller
@RequestMapping(value = "/connect")
public class ConnectController {

    private static final Logger logger = LoggerFactory.getLogger(ConnectController.class);
    private final GarminService garminService;

    @Autowired
    public ConnectController(GarminService garminService) {
        this.garminService = garminService;
    }

    @RequestMapping(value = "auth/{uuid}", method = RequestMethod.GET)
    public ModelAndView connectRedirect(@PathVariable("uuid") String uuid) throws Exception {
        return new ModelAndView("redirect:" + this.garminService.generateAuthenticationUrl(uuid));
    }

    /**
     * this call is performed after the garmin callback
     */
    @RequestMapping(value = "auth/{uuid}/receive", method = RequestMethod.GET)
    public ModelAndView connectReceive(@RequestParam("oauth_token") String oauthToken, @RequestParam(value = "oauth_verifier", required = false) String oauthVerifier, @PathVariable("uuid") String uuid) throws Exception {
        try {
            logger.debug("connectReceive START");
            this.garminService.requestAccessToken(oauthVerifier, uuid);
        } catch (GarminConnectException e) {
            if (e.getError() == GarminConnectError.GARMIN_TOKEN_ALREADY_ASSOCIATED) {
                return new ModelAndView("redirect:/tokenalreadyassociated.html");
            } else throw e;
        }
        return new ModelAndView("redirect:/confirm.html");
    }

    @RequestMapping(value = "push", method = RequestMethod.POST)
    public ResponseEntity pushPost(@RequestBody String payload, HttpServletRequest request) {
        this.garminService.handlePayload(payload);
        return ResponseEntity.ok(true);
    }
}
