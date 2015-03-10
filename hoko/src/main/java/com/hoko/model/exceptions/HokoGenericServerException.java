package com.hoko.model.exceptions;

public class HokoGenericServerException extends HokoException {
    public HokoGenericServerException() {
        super(5, "Could not reach the Hoko Service. Please try again later.");
    }
}