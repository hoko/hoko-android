package com.hokolinks.model;

import android.content.Context;

import com.hokolinks.deeplinking.listeners.MetadataRequestListener;
import com.hokolinks.utils.Utils;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.networking.async.HttpRequest;
import com.hokolinks.utils.networking.async.HttpRequestCallback;
import com.hokolinks.utils.networking.async.NetworkAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Deeplink is the model which represents an inbound or outbound deeplink object.
 * It contains a route format string, the route parameters, the query parameters and an optional
 * url scheme.
 */
public class Deeplink {

    // Key values from incoming deeplinks
    private static final String SMARTLINK_CLICK_IDENTIFIER_KEY = "_hk_cid";
    private static final String METADATA_KEY = "_hk_cid";
    private static final String METADATA_PATH = "smartlinks/%s/metadata";

    private String mRoute;
    private HashMap<String, String> mRouteParameters;
    private HashMap<String, String> mQueryParameters;
    private JSONObject mMetadata;
    private String mURLScheme;
    private HashMap<String, JSONObject> mURLs;
    private String mDeeplinkURL;

    /**
     * The constructor for Deeplink objects.
     *
     * @param urlScheme       Optional url scheme.
     * @param route           A route in route format.
     * @param routeParameters A HashMap where the keys are the route components and the values are
     *                        the route parameters.
     * @param queryParameters A HashMap where the keys are the query components and the values are
     *                        the query parameters.
     * @param metadata A JSONObject containing metadata to be passed to whoever opens the deeplink.
     * @param deeplinkURL The actual deeplink url opened by the app.
     */
    public Deeplink(String urlScheme, String route, HashMap<String, String> routeParameters,
                    HashMap<String, String> queryParameters, JSONObject metadata, String deeplinkURL) {
        if (urlScheme == null)
            mURLScheme = "";
        else
            mURLScheme = urlScheme;

        mRoute = route;
        mMetadata = metadata;
        mRouteParameters = routeParameters != null ? routeParameters : new HashMap<String, String>();
        mQueryParameters = queryParameters != null ? queryParameters : new HashMap<String, String>();
        mURLs = new HashMap<>();
        mDeeplinkURL = deeplinkURL;
    }

    /**
     * An easy to use static function for the developer to generate their own deeplinks to
     * generate Smartlinks afterwards.
     *
     * @return The generated Deeplink.
     */
    public static Deeplink deeplink() {
        return deeplink(null);
    }

    /**
     * An easy to use static function for the developer to generate their own deeplinks to
     * generate Smartlinks afterwards.
     *
     * @param route           A route in route format.
     * @return The generated Deeplink.
     */
    public static Deeplink deeplink(String route) {
        return deeplink(route, null);
    }

    /**
     * An easy to use static function for the developer to generate their own deeplinks to
     * generate Smartlinks afterwards.
     *
     * @param route           A route in route format.
     * @param routeParameters A HashMap where the keys are the route components and the values are
     *                        the route parameters.
     * @return The generated Deeplink.
     */
    public static Deeplink deeplink(String route, HashMap<String, String> routeParameters) {
        return deeplink(route, routeParameters, null);
    }

    /**
     * An easy to use static function for the developer to generate their own deeplinks to
     * generate Smartlinks afterwards.
     *
     * @param route           A route in route format.
     * @param routeParameters A HashMap where the keys are the route components and the values are
     *                        the route parameters.
     * @param queryParameters A HashMap where the keys are the query components and the values are
     *                        the query parameters.
     * @return The generated Deeplink.
     */
    public static Deeplink deeplink(String route, HashMap<String, String> routeParameters,
                                        HashMap<String, String> queryParameters) {
        return deeplink(route, routeParameters, queryParameters, null);
    }

    /**
     * An easy to use static function for the developer to generate their own deeplinks to
     * generate Smartlinks afterwards.
     *
     * @param route           A route in route format.
     * @param routeParameters A HashMap where the keys are the route components and the values are
     *                        the route parameters.
     * @param queryParameters A HashMap where the keys are the query components and the values are
     *                        the query parameters.
     * @param metadata A JSONObject containing metadata to be passed to whoever opens the deeplink.
     * @return The generated Deeplink.
     */
    public static Deeplink deeplink(String route, HashMap<String, String> routeParameters,
                                    HashMap<String, String> queryParameters, JSONObject metadata) {
        Deeplink deeplink = new Deeplink(null, Utils.sanitizeRoute(route),
                routeParameters, queryParameters, metadata, null);

        if (matchRoute(deeplink.getRoute(), deeplink.getRouteParameters()) ||
                (route == null && routeParameters == null && queryParameters == null &&
                        metadata == null)) {
            return deeplink;
        }
        return null;
    }

    public static boolean matchRoute(String route, HashMap<String, String> routeParameters) {
        List<String> routeComponents = Arrays.asList(route.split("/"));
        for (int index = 0; index < routeComponents.size(); index++) {
            String routeComponent = routeComponents.get(index);

            if (routeComponent.startsWith(":") && routeComponent.length() > 2) {
                String token = routeComponent.substring(1);
                if (!routeParameters.containsKey(token)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Allows the developer to add a custom deeplink for a given platform.
     *
     * @param url      The deeplink URL to be used on the platform.
     * @param platform The platform (from the DeeplinkPlatform enum).
     */
    public void addURL(String url, DeeplinkPlatform platform) {
        try {
            JSONObject urlJSON = new JSONObject();
            urlJSON.put("link", url);
            mURLs.put(stringForPlatform(platform), urlJSON);
        } catch (JSONException e) {
            HokoLog.e(e);
        }
    }

    /**
     * Logic behind the deeplink needing to request the server for metadata.
     *
     * @return true if the server should get metadata and doesn't have it already, false otherwise.
     */
    public boolean needsMetadata() {
        return hasMetadataKey() && mMetadata == null;
    }

    private String stringForPlatform(DeeplinkPlatform platform) {
        switch (platform) {
            case IPHONE:
                return "iphone";
            case IPAD:
                return "ipad";
            case IOS_UNIVERSAL:
                return "universal";
            case ANDROID:
                return "android";
            case WEB:
                return "web";
            default:
                return null;
        }
    }

    private boolean hasURLs() {
        return mURLs.size() > 0;
    }

    /**
     * This function serves the purpose of communicating to the Hoko backend service that a given
     * inbound deeplink object was opened either through the notification id or through the
     * deeplink id.
     *
     * @param token   The Hoko API Token.
     */
    public void post(String token, Context context) {
        if (isSmartlink()) {
            Networking.getNetworking().addRequest(
                    new HttpRequest(HttpRequest.HokoNetworkOperationType.POST,
                            "smartlinks/open", token,
                            smartlinkJSON(context).toString()));
        }

    }

    /**
     * Requests metadata for the Deeplink object from the HOKO server.
     * Will call the listener after the request is complete.
     * @param token The HOKO SDK token.
     * @param metadataRequestListener A listener to know when the task completes.
     */
    public void requestMetadata(String token, final MetadataRequestListener metadataRequestListener) {
        if (needsMetadata()) {
            String path = String.format(METADATA_PATH, getSmartlinkClickIdentifier());
            Networking.getNetworking().addRequest(
                    new HttpRequest(HttpRequest.HokoNetworkOperationType.GET,
                            HttpRequest.getURLFromPath(path), token, null));
            new NetworkAsyncTask(new HttpRequest(HttpRequest.HokoNetworkOperationType.GET,
                    HttpRequest.getURLFromPath(path), token, null)
                    .toRunnable(new HttpRequestCallback() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            Deeplink.this.mMetadata = jsonObject;
                            if (metadataRequestListener != null) {
                                metadataRequestListener.completion();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (metadataRequestListener != null) {
                                metadataRequestListener.completion();
                            }
                        }
                    })).execute();
        }
    }

    private String getURL() {
        String url = this.getRoute();
        if (this.getRouteParameters() != null) {
            for (String routeParameterKey : this.getRouteParameters().keySet()) {
                url = url.replace(":" + routeParameterKey, this.getRouteParameters()
                        .get(routeParameterKey));
            }
        }
        if (this.getRouteParameters() != null && this.getQueryParameters().size() > 0) {
            url = url + "?";
            for (String queryParameterKey : this.getQueryParameters().keySet()) {
                url = url + queryParameterKey + "=" + this.getQueryParameters()
                        .get(queryParameterKey) + "&";
            }
            url = url.substring(url.length() - 1);
        }
        return url;
    }

    /**
     * Serves the purpose of returning a Deeplink in JSON form (useful for PhoneGap SDK)
     *
     * @return Deeplink in JSON form.
     */
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("route", getRoute());
            jsonObject.put("routeParameters", new JSONObject(mRouteParameters));
            jsonObject.put("queryParameters", new JSONObject(mQueryParameters));
            jsonObject.putOpt("metadata", getMetadata());
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return jsonObject;
    }

    /**
     * Converts all the Deeplink information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @return The JSONObject representation of Deeplink.
     */
    public JSONObject json() {
        try {
            JSONObject root = new JSONObject();
            root.putOpt("uri", getURL());
            root.putOpt("metadata", getMetadata());
            if (hasURLs())
                root.putOpt("routes", new JSONObject(mURLs));
            return root;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Converts the Deeplink into a JSONObject referring the Smartlink that was opened.
     *
     * @return The JSONObject representation of the Smartlink.
     */
    private JSONObject smartlinkJSON(Context context) {
        JSONObject root = new JSONObject();
        try {
            root.put("deeplink", mDeeplinkURL);
            root.put("uid", Device.getDeviceID(context));
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return root;
    }

    public String toString() {
        String urlScheme = mURLScheme != null ? mURLScheme : "";
        String route = mRoute != null ? mRoute : "";
        String routeParameters = mRouteParameters != null ? mRouteParameters.toString() : "";
        String queryParameters = mQueryParameters != null ? mQueryParameters.toString() : "";
        String metadata = mMetadata != null ? mMetadata.toString() : "";
        return "<Deeplink> URLScheme='" + urlScheme + "' route ='" + route
                + "' routeParameters='" + routeParameters + "' queryParameters='" + queryParameters
                + "' metadata='" + metadata + "'";
    }

    public String getURLScheme() {
        return mURLScheme;
    }

    public HashMap<String, String> getRouteParameters() {
        return mRouteParameters;
    }

    public HashMap<String, String> getQueryParameters() {
        return mQueryParameters;
    }

    public JSONObject getMetadata() {
        return mMetadata;
    }

    public void setMetadata(JSONObject metadata) {
        mMetadata = metadata;
    }

    public String getRoute() {
        return mRoute;
    }

    private String getSmartlinkClickIdentifier() {
        return mQueryParameters.get(SMARTLINK_CLICK_IDENTIFIER_KEY);
    }

    private boolean hasMetadataKey() {
        return mQueryParameters.containsKey(METADATA_KEY);
    }

    private boolean isSmartlink() {
        return getSmartlinkClickIdentifier() != null;
    }

}
