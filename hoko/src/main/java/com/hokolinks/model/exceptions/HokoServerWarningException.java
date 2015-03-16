package com.hokolinks.model.exceptions;

public class HokoServerWarningException extends HokoException {
    public HokoServerWarningException(int code, String message) {
        super(code, "Warning " + message);
    }
}