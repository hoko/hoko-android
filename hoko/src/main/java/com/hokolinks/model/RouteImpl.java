package com.hokolinks.model;

public class RouteImpl extends Route {

    private DeeplinkCallback mDeeplinkCallback;

    public RouteImpl(String route, DeeplinkCallback deeplinkCallback) {
        super(route);
        mDeeplinkCallback = deeplinkCallback;
    }

    public RouteImpl(String route, boolean internal, DeeplinkCallback deeplinkCallback) {
        super(route, internal);
        mDeeplinkCallback = deeplinkCallback;
    }

    @Override
    public void execute(Deeplink deeplink) {
        if (mDeeplinkCallback != null) {
            mDeeplinkCallback.deeplinkOpened(deeplink);
        }
    }
}
