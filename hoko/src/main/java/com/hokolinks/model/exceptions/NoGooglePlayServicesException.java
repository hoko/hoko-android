package com.hokolinks.model.exceptions;

public class NoGooglePlayServicesException extends HokoException {
    public NoGooglePlayServicesException() {
        super(8, "Google Play services are not available on this device.");
    }
}