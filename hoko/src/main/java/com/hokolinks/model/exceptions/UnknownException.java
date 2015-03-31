package com.hokolinks.model.exceptions;

public class UnknownException extends HokoException {
    public UnknownException() {
        super(0, "Unknown error.");
    }
}
