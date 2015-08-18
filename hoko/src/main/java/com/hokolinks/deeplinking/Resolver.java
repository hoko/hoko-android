package com.hokolinks.deeplinking;

import android.content.Context;

import com.hokolinks.deeplinking.listeners.SmartlinkResolveListener;
import com.hokolinks.model.Device;
import com.hokolinks.model.exceptions.LinkResolveException;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

class Resolver {

    private static final String RESOLVER_ENDPOINT = "smartlinks/resolve";

    private String mToken;
    private Context mContext;

    public Resolver(String token, Context context) {
        mToken = token;
        mContext = context;
    }

    public void resolveSmartlink(String smartlink, final SmartlinkResolveListener resolveListener) {
        new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.POST,
                RESOLVER_ENDPOINT, mToken, json(smartlink, mContext).toString())
                .toRunnable(new HttpRequestCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        String deeplink = jsonObject.optString("deeplink");
                        JSONObject metadata = jsonObject.optJSONObject("metadata");
                        if (resolveListener != null) {
                            if (deeplink != null)
                                resolveListener.onLinkResolved(deeplink, metadata);
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

    private JSONObject json(String smartlink, Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("smartlink", smartlink);
            jsonObject.put("uid", Device.getDeviceID(context));
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return jsonObject;
    }


}
