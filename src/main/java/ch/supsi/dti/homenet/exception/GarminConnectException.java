package ch.supsi.dti.homenet.exception;

import ch.supsi.dti.homenet.enums.GarminConnectError;

public class GarminConnectException extends Exception {
    private GarminConnectError error;
    private String details;
    private Throwable e;

    public GarminConnectException(GarminConnectError error) {
        super(error.getMessage());
        this.error = error;
    }

    public GarminConnectException(GarminConnectError error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
        this.e = e;
    }

    public GarminConnectException(GarminConnectError error, String details) {
        super(error.getMessage());
        this.error = error;
        this.details = details;
    }

    public GarminConnectError getError() {
        return this.error;
    }

    public String getDetails() {
        if (this.details == null && this.e != null)
            return e.getMessage();
        else
            return details;
    }
}
