package com.hokolinks.model.exceptions;

public class SetupNotCalledYetException extends HokoException {
    public SetupNotCalledYetException() {
        super(1, "Cannot access modules without calling Hoko.setup(...) beforehand.");
    }
}
