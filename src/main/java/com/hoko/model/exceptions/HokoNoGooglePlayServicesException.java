package com.hoko.model.exceptions;

public class HokoNoGooglePlayServicesException extends HokoException {
    public HokoNoGooglePlayServicesException() {
        super(8, "Google Play services are not available on this device.");
    }
}