package com.hokolinks.deeplinking;

import com.hokolinks.Hoko;
import com.hokolinks.deeplinking.listeners.HokoLinkGenerationListener;
import com.hokolinks.model.HokoDeeplink;
import com.hokolinks.model.exceptions.HokoLinkGenerationException;
import com.hokolinks.model.exceptions.HokoNullDeeplinkException;
import com.hokolinks.model.exceptions.HokoRouteNotMappedException;
import com.hokolinks.utils.networking.async.HokoAsyncTask;
import com.hokolinks.utils.networking.async.HokoHttpRequest;
import com.hokolinks.utils.networking.async.HokoHttpRequestCallback;

import org.json.JSONObject;

/**
 * HokoLinkGenerator serves the purpose of generating Hokolinks for a given deeplink.
 * It connects with the Hoko backend service and will return a http link which will redirect
 * according to the correct deeplink depending on the platform it is later opened.
 */
public class HokoLinkGenerator {

    private String mToken;

    public HokoLinkGenerator(String token) {
        mToken = token;
    }

    /**
     * Validates deeplinks before actually trying to generate them through a Hoko backend service
     * call.
     *
     * @param deeplink A user generated deeplink or an annotation based deeplink.
     * @param listener A HokoLinkGenerationListener instance.
     */
    public void generateHokolink(HokoDeeplink deeplink, HokoLinkGenerationListener listener) {
        if (deeplink == null) {
            listener.onError(new HokoNullDeeplinkException());
        } else if (!Hoko.deeplinking().routing().routeExists(deeplink.getRoute())) {
            listener.onError(new HokoRouteNotMappedException());
        } else {
            requestForDeeplink(deeplink, listener);
        }
    }

    /**
     * Performs a request to the Hoko backend service to translate a deeplink into an Hokolink.
     * Calls the listener depending on the success or failure of such a network call.
     *
     * @param deeplink A user generated deeplink or an annotation based deeplink.
     * @param listener A HokoLinkGenerationListener instance.
     */
    private void requestForDeeplink(HokoDeeplink deeplink,
                                    final HokoLinkGenerationListener listener) {
        //TODO Change to omnilinks
        new HokoAsyncTask(new HokoHttpRequest(HokoHttpRequest.HokoNetworkOperationType.POST,
                "omnilinks", mToken, deeplink.json().toString())
                .toRunnable(new HokoHttpRequestCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        String hokolink = jsonObject.optString("omnilink");
                        if (listener != null) {
                            if (hokolink != null)
                                listener.onLinkGenerated(hokolink);
                            else
                                listener.onError(new HokoLinkGenerationException());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (listener != null)
                            listener.onError(new HokoLinkGenerationException());
                    }
                })).execute();
    }

}
