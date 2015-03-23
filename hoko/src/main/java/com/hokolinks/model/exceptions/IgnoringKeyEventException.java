package com.hokolinks.model.exceptions;

import com.hokolinks.model.Event;

public class IgnoringKeyEventException extends HokoException {
    public IgnoringKeyEventException(Event event) {
        super(9, "Ignoring key event " + event.toString()
                + " because there is no deeplinking session.");
    }

}
