package com.hoko.model.exceptions;

public class HokoActivityNotDeeplinkableException extends HokoException {
    public HokoActivityNotDeeplinkableException(String className) {
        super(11, className + " does not have the DeeplinkRoute annotation.");
    }
}
