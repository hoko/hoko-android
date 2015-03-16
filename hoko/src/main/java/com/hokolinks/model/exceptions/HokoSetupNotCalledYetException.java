package com.hokolinks.model.exceptions;

public class HokoSetupNotCalledYetException extends HokoException {
    public HokoSetupNotCalledYetException() {
        super(1, "Cannot access modules without calling Hoko.setup(...) beforehand.");
    }
}
