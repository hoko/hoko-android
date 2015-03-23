package com.hokolinks.deeplinking.listeners;

import com.hokolinks.model.Deeplink;

/**
 * Handler is an interface to be used when your app needs to know when a deeplink has been
 * opened.
 * Its primary use is to add on to the Deeplinking module, which will then execute it when
 * deeplinks are opened.
 */
public interface Handler {
    void handle(Deeplink deeplink);
}
