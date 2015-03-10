package com.hoko.model.exceptions;

public class HokoSetupCalledMoreThanOnceException extends HokoException {
    public HokoSetupCalledMoreThanOnceException() {
        super(2, "Cannot call the Hoko.setup(...) methods more than once on the application's "
                + "lifecycle.");
    }
}
