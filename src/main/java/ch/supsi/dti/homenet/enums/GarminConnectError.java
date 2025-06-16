package ch.supsi.dti.homenet.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GarminConnectError {
    GARMIN_TOKEN_ALREADY_ASSOCIATED(HttpStatus.CONFLICT),
    TIME_OUT(HttpStatus.REQUEST_TIMEOUT),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    NOT_YET_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED),
    GENERIC_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    PARAMETER_MISSING(HttpStatus.BAD_REQUEST),
    INVALID_ENTRY_TYPE(HttpStatus.BAD_REQUEST),

    UUID_NOT_FOUND(HttpStatus.NOT_FOUND),
    UAT_NOT_FOUND(HttpStatus.NOT_FOUND),
    UUID_AUTHORIZATION_NOT_GRANTED(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS),
    UUID_ALREADY_ASSOCIATED(HttpStatus.CONFLICT),
    SMARTGOAL_NOT_SUBSCRIBED(HttpStatus.NOT_FOUND),
    SYNC_DATE_NOT_FOUND(HttpStatus.NOT_FOUND),
    ONBOARDING_QUESTIONNAIRE_ALREADY_COMPLETED(HttpStatus.CONFLICT),
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND),
    UUID_CONFLICT_MARKER_DELETION(HttpStatus.CONFLICT);
    private final HttpStatus httpStatus;

    GarminConnectError(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return this.name();
    }

}
