package com.hoko.model.exceptions;

public class HokoLinkGenerationException extends HokoException {
    public HokoLinkGenerationException() {
        super(4, "Could not generate Hokolink. Please try again later.");
    }
}