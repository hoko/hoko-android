package com.hokolinks.model.exceptions;

public class LinkResolveException extends HokoException {
    public LinkResolveException() {
        super(12, "Could not generate Smartlink. Please try again later.");
    }
}