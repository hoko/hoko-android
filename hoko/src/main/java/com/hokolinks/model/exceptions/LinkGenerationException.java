package com.hokolinks.model.exceptions;

public class LinkGenerationException extends HokoException {
    public LinkGenerationException() {
        super(4, "Could not generate Smartlink. Please try again later.");
    }
}