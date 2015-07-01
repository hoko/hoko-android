package com.hokolinks.model.exceptions;

public class InvalidRouteException extends HokoException {
    public InvalidRouteException(String className, String route) {
        super(13, className + " with route " + route + " does not have the required route parameters mapped.");
    }
}
