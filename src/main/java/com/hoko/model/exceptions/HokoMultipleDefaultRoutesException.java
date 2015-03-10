package com.hoko.model.exceptions;

public class HokoMultipleDefaultRoutesException extends HokoException {
    public HokoMultipleDefaultRoutesException(String className) {
        super(10, "Ignoring multiple default route on activity " + className + ".");
    }
}
