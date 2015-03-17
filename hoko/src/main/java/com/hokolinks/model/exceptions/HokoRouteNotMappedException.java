package com.hokolinks.model.exceptions;

public class HokoRouteNotMappedException extends HokoException {
    public HokoRouteNotMappedException() {
        super(6, "The route is not mapped. Please map it in your Application class before trying"
                + " to generate an Smartlink.");
    }
}