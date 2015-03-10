package com.hoko.deeplinking.listeners;

import com.hoko.model.HokoDeeplink;

/**
 * HokoHandler is an interface to be used when your app needs to know when a deeplink has been opened.
 * Its primary use is to add on to the HokoDeeplinking module, which will then execute it when
 * deeplinks are opened.
 */
public interface HokoHandler {
    public void handle(HokoDeeplink deeplink);
}
