package com.hokolinks.deeplinking.listeners;

import org.json.JSONObject;

public interface SmartlinkResolveListener {

    void onLinkResolved(String deeplink, JSONObject metadata);

    void onError(Exception e);

}
