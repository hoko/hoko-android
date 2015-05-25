package com.hokolinks.deeplinking.listeners;

public interface SmartlinkResolveListener {

    void onLinkResolved(String deeplink);

    void onError(Exception e);

}
