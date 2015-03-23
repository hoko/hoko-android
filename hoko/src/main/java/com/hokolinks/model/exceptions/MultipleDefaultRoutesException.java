package com.hokolinks.model.exceptions;

public class MultipleDefaultRoutesException extends HokoException {
    public MultipleDefaultRoutesException(String className) {
        super(10, "Ignoring multiple default route on activity " + className + ".");
    }
}
