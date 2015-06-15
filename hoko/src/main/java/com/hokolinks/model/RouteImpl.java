package com.hokolinks.model;

import java.util.HashMap;

public class RouteImpl extends Route {

    DeeplinkCallback mDeeplinkCallback;

    public RouteImpl(String route, DeeplinkCallback deeplinkCallback) {
        super(route);
        mDeeplinkCallback = deeplinkCallback;
    }

    @Override
    public void execute(URL url) {
        if (mDeeplinkCallback != null) {
            HashMap<String, String> routeParameters = url.matchesWithRoute(this);
            if (routeParameters != null) {
                Deeplink deeplink = new Deeplink(url.getScheme(), this.getRoute(),
                        routeParameters, url.getQueryParameters(), url.getURL());
                mDeeplinkCallback.deeplinkOpened(deeplink);
            }
        }
    }
}
