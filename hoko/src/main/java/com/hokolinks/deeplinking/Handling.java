package com.hokolinks.deeplinking;

import com.hokolinks.model.Deeplink;
import com.hokolinks.model.DeeplinkCallback;

import java.util.ArrayList;

/**
 * This class serves the purpose of abstracting the call of handlers from the Deeplinking
 * module, it calls the handlers in the order they were added.
 */
public class Handling {

    private ArrayList<DeeplinkCallback> mHandlers;

    public Handling() {
        mHandlers = new ArrayList<>();
    }

    /**
     * Adds a DeeplinkCallback object to the registered handlers.
     *
     * @param callback A DeeplinkCallback object.
     */
    public void addHandler(DeeplinkCallback callback) {
        mHandlers.add(callback);
    }

    /**
     * Removes a DeeplinkCallback object from the registered handlers.
     *
     * @param callback A DeeplinkCallback object.
     * @return true if handler was removed, false otherwise.
     */
    public boolean removeHandler(DeeplinkCallback callback) {
        return mHandlers.remove(callback);
    }

    /**
     * Delegates the deeplink to all the handlers registered.
     *
     * @param deeplink A deeplink object.
     */
    public void handle(Deeplink deeplink) {
        for (DeeplinkCallback handler : mHandlers) {
            handler.deeplinkOpened(deeplink);
        }
    }

}
