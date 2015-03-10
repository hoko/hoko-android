package com.hoko.model.exceptions;

public class HokoNullDeeplinkException extends HokoException {
    public HokoNullDeeplinkException() {
        super(7, "Deeplink provided was null. Be sure the route format and route parameters are "
                + "correct when creating a HokoDeeplink object.");
    }
}