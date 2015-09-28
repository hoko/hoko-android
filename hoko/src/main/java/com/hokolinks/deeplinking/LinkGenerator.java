package com.hokolinks.deeplinking;

import com.hokolinks.Hoko;
import com.hokolinks.deeplinking.listeners.LinkGenerationListener;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.exceptions.InvalidDomainException;
import com.hokolinks.model.exceptions.LazySmartlinkCantHaveURLsException;
import com.hokolinks.model.exceptions.LinkGenerationException;
import com.hokolinks.model.exceptions.NullDeeplinkException;
import com.hokolinks.model.exceptions.RouteNotMappedException;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * LinkGenerator serves the purpose of generating Smartlinks for a given deeplink.
 * It connects with the Hoko backend service and will return a http link which will redirect
 * according to the correct deeplink depending on the platform it is later opened.
 */
class LinkGenerator {

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
    public void generateSmartlink(Deeplink deeplink, LinkGenerationListener listener) {
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
        new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.POST,
                "smartlinks", mToken, deeplink.json().toString())
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

    /**
     *  generateLazySmartlink(deeplink, domain) allows the app to generate lazy Smartlinks for the
     *  user to share with other users, independent of the platform, users will be redirected to the
     *  corresponding view. A user generated Deeplink object may be passed along to generate the
     *  deeplinks for all available platforms. In case the translation is possible, the method will
     *  return a lazy Smartlink (e.g. http://yourapp.hoko.link/lazy?uri=%2Fproduct%2F0 ).
     *  Where the uri query parameter will be the url encoded version of the translated deep link.
     *
     * @param deeplink A Deeplink object.
     * @param domain   The domain to which HOKO should generate a lazy Smartlink.
     *                 (e.g. yourapp.hoko.link or yourapp.customdomain.com).
     */
    String generateLazySmartlink(Deeplink deeplink, String domain) {
        if (deeplink != null && domain != null) {
            if (deeplink.hasURLs()) {
                HokoLog.e(new LazySmartlinkCantHaveURLsException());
                return null;
            }
            String strippedDomain = domain + "";
            strippedDomain = strippedDomain.replace("http://", "");
            strippedDomain = strippedDomain.replace("https://","");
            if (strippedDomain.contains("/")) {
                HokoLog.e(new InvalidDomainException(domain));
            } else {
                try {
                    return "http://" + domain + "lazy?uri=" + URLEncoder.encode(deeplink.getURL(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    HokoLog.e(e);
                }
            }
        }
        return null;
    }

}
