package com.hokolinks.model;

public class RouteImpl extends Route {

    private DeeplinkCallback mDeeplinkCallback;

    public RouteImpl(String route, DeeplinkCallback deeplinkCallback) {
        super(route);
        mDeeplinkCallback = deeplinkCallback;
    }

    @Override
    public void execute(Deeplink deeplink) {
        if (mDeeplinkCallback != null) {
            mDeeplinkCallback.deeplinkOpened(deeplink);
        }
    }
}
