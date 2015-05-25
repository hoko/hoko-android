package com.hokolinks.deeplinking;

import com.hokolinks.deeplinking.listeners.SmartlinkResolveListener;
import com.hokolinks.model.exceptions.LinkResolveException;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

public class Resolver {

    private static final String RESOLVER_ENDPOINT = "smartlinks/resolve";

    private String mToken;

    public Resolver(String token) {
        mToken = token;
    }

    public void resolveSmartlink(String smartlink, final SmartlinkResolveListener resolveListener) {
        new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.POST,
                RESOLVER_ENDPOINT, mToken, json(smartlink).toString())
                .toRunnable(new HttpRequestCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        String deeplink = jsonObject.optString("deeplink");
                        if (resolveListener != null) {
                            if (deeplink != null)
                                resolveListener.onLinkResolved(deeplink);
                            else
                                resolveListener.onError(new LinkResolveException());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (resolveListener != null)
                            resolveListener.onError(new LinkResolveException());
                    }
                })).execute();

    }

    private JSONObject json(String smartlink) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("smartlink", smartlink);
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return jsonObject;
    }


}
