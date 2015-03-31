package com.hokolinks.deeplinking;

import com.hokolinks.Hoko;
import com.hokolinks.deeplinking.listeners.LinkGenerationListener;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.exceptions.LinkGenerationException;
import com.hokolinks.model.exceptions.NullDeeplinkException;
import com.hokolinks.model.exceptions.RouteNotMappedException;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;
import com.hokolinks.utils.networking.async.HttpRequest;

import org.json.JSONObject;

/**
 * LinkGenerator serves the purpose of generating Smartlinks for a given deeplink.
 * It connects with the Hoko backend service and will return a http link which will redirect
 * according to the correct deeplink depending on the platform it is later opened.
 */
public class LinkGenerator {

    private String mToken;

    public LinkGenerator(String token) {
        mToken = token;
    }

    /**
     * Validates deeplinks before actually trying to generate them through a Hoko backend service
     * call.
     *
     * @param deeplink A user generated deeplink or an annotation based deeplink.
     * @param listener A LinkGenerationListener instance.
     */
    public void generateHokolink(Deeplink deeplink, LinkGenerationListener listener) {
        if (deeplink == null) {
            listener.onError(new NullDeeplinkException());
        } else if (!Hoko.deeplinking().routing().routeExists(deeplink.getRoute())) {
            listener.onError(new RouteNotMappedException());
        } else {
            requestForSmartlink(deeplink, listener);
        }
    }

    /**
     * Performs a request to the Hoko backend service to translate a deeplink into an Smartlink.
     * Calls the listener depending on the success or failure of such a network call.
     *
     * @param deeplink A user generated deeplink or an annotation based deeplink.
     * @param listener A LinkGenerationListener instance.
     */
    private void requestForSmartlink(Deeplink deeplink,
                                     final LinkGenerationListener listener) {
        String path = deeplink.hasURLs() ? "smartlinks/create_custom"
                : "smartlinks/create_with_template";
        new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.POST,
                path, mToken, deeplink.json().toString())
                .toRunnable(new HttpRequestCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        String smartlink = jsonObject.optString("smartlink");
                        if (listener != null) {
                            if (smartlink != null)
                                listener.onLinkGenerated(smartlink);
                            else
                                listener.onError(new LinkGenerationException());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (listener != null)
                            listener.onError(new LinkGenerationException());
                    }
                })).execute();
    }

}
