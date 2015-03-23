package com.hokolinks.model.exceptions;

public class SetupCalledMoreThanOnceException extends HokoException {
    public SetupCalledMoreThanOnceException() {
        super(2, "Cannot call the Hoko.setup(...) methods more than once on the application's "
                + "lifecycle.");
    }
}
