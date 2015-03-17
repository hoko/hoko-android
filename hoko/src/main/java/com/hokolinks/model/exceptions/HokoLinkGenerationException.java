package com.hokolinks.model.exceptions;

public class HokoLinkGenerationException extends HokoException {
    public HokoLinkGenerationException() {
        super(4, "Could not generate Smartlink. Please try again later.");
    }
}