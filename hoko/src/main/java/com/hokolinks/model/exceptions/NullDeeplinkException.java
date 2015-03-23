package com.hokolinks.model.exceptions;

public class NullDeeplinkException extends HokoException {
    public NullDeeplinkException() {
        super(7, "Deeplink provided was null. Be sure the route format and route parameters are "
                + "correct when creating a Deeplink object.");
    }
}