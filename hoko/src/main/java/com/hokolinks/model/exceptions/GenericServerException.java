package com.hokolinks.model.exceptions;

public class GenericServerException extends HokoException {
    public GenericServerException() {
        super(5, "Could not reach the Hoko Service. Please try again later.");
    }
}