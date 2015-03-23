package com.hokolinks.model.exceptions;

public class ServerWarningException extends HokoException {
    public ServerWarningException(int code, String message) {
        super(code, "Warning " + message);
    }
}