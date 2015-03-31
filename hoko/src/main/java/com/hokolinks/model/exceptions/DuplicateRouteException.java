package com.hokolinks.model.exceptions;

public class DuplicateRouteException extends HokoException {
    public DuplicateRouteException(String route) {
        super(3, "The route " + route + " will be ignored as it was already mapped before.");
    }
}