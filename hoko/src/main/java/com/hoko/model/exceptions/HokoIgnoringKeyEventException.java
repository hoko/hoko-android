package com.hoko.model.exceptions;

import com.hoko.model.HokoEvent;

public class HokoIgnoringKeyEventException extends HokoException {
    public HokoIgnoringKeyEventException(HokoEvent event) {
        super(9, "Ignoring key event " + event.toString()
                + " because there is no deeplinking session.");
    }

}
