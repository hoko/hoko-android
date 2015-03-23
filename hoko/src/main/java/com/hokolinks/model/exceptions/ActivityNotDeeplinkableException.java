package com.hokolinks.model.exceptions;

public class ActivityNotDeeplinkableException extends HokoException {
    public ActivityNotDeeplinkableException(String className) {
        super(11, className + " does not have the DeeplinkRoute annotation.");
    }
}
