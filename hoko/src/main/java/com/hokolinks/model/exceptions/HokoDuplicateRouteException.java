package com.hokolinks.model.exceptions;

public class HokoDuplicateRouteException extends HokoException {
    public HokoDuplicateRouteException(String route) {
        super(3, "The route " + route + " will be ignored as it was already mapped before.");
    }
}