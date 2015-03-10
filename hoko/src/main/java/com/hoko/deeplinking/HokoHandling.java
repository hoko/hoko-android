package com.hoko.deeplinking;

import com.hoko.deeplinking.listeners.HokoHandler;
import com.hoko.model.HokoDeeplink;

import java.util.ArrayList;

/**
 * This class serves the purpose of abstracting the call of handlers from the HokoDeeplinking
 * module, it calls the handlers in the order they were added.
 */
public class HokoHandling {

    private ArrayList<HokoHandler> mHandlers;

    public HokoHandling() {
        mHandlers = new ArrayList<HokoHandler>();
    }

    /**
     * Adds a HokoHandler object to the registered handlers.
     *
     * @param handler A HokoHandler object.
     */
    public void addHandler(HokoHandler handler) {
        mHandlers.add(handler);
    }

    /**
     * Removes a HokoHandler object from the registered handlers.
     *
     * @param handler A HokoHandler object.
     * @return true if handler was removed, false otherwise.
     */
    public boolean removeHandler(HokoHandler handler) {
        return mHandlers.remove(handler);
    }

    /**
     * Delegates the deeplink to all the handlers registered.
     *
     * @param deeplink A deeplink object.
     */
    public void handle(HokoDeeplink deeplink) {
        for (HokoHandler handler : mHandlers) {
            handler.handle(deeplink);
        }
    }

}
