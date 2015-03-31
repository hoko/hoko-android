package com.hokolinks.model.exceptions;


public class ServerErrorException extends HokoException {
    public ServerErrorException(int code, String message) {
        super(code, "Error " + message);
    }
}