package com.hokolinks.model.exceptions;

public class RouteNotMappedException extends HokoException {
    public RouteNotMappedException() {
        super(6, "The route is not mapped. Please map it in your Application class before trying"
                + " to generate an Smartlink.");
    }
}