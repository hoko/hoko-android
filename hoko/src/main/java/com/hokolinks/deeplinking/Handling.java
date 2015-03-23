package com.hokolinks.deeplinking;

import com.hokolinks.deeplinking.listeners.Handler;
import com.hokolinks.model.Deeplink;

import java.util.ArrayList;

/**
 * This class serves the purpose of abstracting the call of handlers from the Deeplinking
 * module, it calls the handlers in the order they were added.
 */
public class Handling {

    private ArrayList<Handler> mHandlers;

    public Handling() {
        mHandlers = new ArrayList<Handler>();
    }

    /**
     * Adds a Handler object to the registered handlers.
     *
     * @param handler A Handler object.
     */
    public void addHandler(Handler handler) {
        mHandlers.add(handler);
    }

    /**
     * Removes a Handler object from the registered handlers.
     *
     * @param handler A Handler object.
     * @return true if handler was removed, false otherwise.
     */
    public boolean removeHandler(Handler handler) {
        return mHandlers.remove(handler);
    }

    /**
     * Delegates the deeplink to all the handlers registered.
     *
     * @param deeplink A deeplink object.
     */
    public void handle(Deeplink deeplink) {
        for (Handler handler : mHandlers) {
            handler.handle(deeplink);
        }
    }

}
