package com.hokolinks.model.exceptions;

public class LazySmartlinkCantHaveURLsException extends HokoException {
    public LazySmartlinkCantHaveURLsException() {
        super(14, "Lazy smartlinks cannot have custom URLs for each platform.");
    }
}
