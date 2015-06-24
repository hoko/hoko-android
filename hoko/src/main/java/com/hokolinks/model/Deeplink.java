package com.hokolinks.model;

import com.hokolinks.utils.Utils;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.networking.async.HttpRequest;

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
    public static final String HokoDeeplinkSmartlinkClickIdentifierKey = "_hk_cid";

    private String mRoute;
    private HashMap<String, String> mRouteParameters;
    private HashMap<String, String> mQueryParameters;
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
     * @param deeplinkURL The actual deeplink url opened by the app.
     */
    public Deeplink(String urlScheme, String route, HashMap<String, String> routeParameters,
                    HashMap<String, String> queryParameters, String deeplinkURL) {
        if (urlScheme == null)
            mURLScheme = "";
        else
            mURLScheme = urlScheme;

        mRoute = route;
        mRouteParameters = routeParameters;
        mQueryParameters = queryParameters;
        mURLs = new HashMap<>();
        mDeeplinkURL = deeplinkURL;
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
        Deeplink deeplink = new Deeplink(null, Utils.sanitizeRoute(route),
                routeParameters, queryParameters, null);

        if (Deeplink.matchRoute(deeplink.getRoute(), deeplink.getRouteParameters())) {
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

    private String stringForPlatform(DeeplinkPlatform platform) {
        switch (platform) {
            case IPHONE:
                return "iphone";
            case IPAD:
                return "ipad";
            case IOS_UNIVERSAL:
                return "ios";
            case ANDROID:
                return "android";
            case WEB:
                return "web";
            default:
                return null;
        }
    }

    public boolean hasURLs() {
        return mURLs.size() > 0;
    }

    /**
     * This function serves the purpose of communicating to the Hoko backend service that a given
     * inbound deeplink object was opened either through the notification id or through the
     * deeplink id.
     *
     * @param token   The Hoko API Token.
     */
    public void post(String token) {
        if (isSmartlink()) {
            Networking.getNetworking().addRequest(
                    new HttpRequest(HttpRequest.HokoNetworkOperationType.POST,
                            "smartlinks/open", token,
                            smartlinkJSON().toString()));
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
            if (mURLs.size() > 0)
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
    private JSONObject smartlinkJSON() {
        try {
            JSONObject root = new JSONObject();
            root.put("deeplink", mDeeplinkURL);
            return root;
        } catch (JSONException e) {
            return null;
        }
    }

    public String toString() {
        String urlScheme = mURLScheme != null ? mURLScheme : "";
        String route = mRoute != null ? mRoute : "";
        String routeParameters = mRouteParameters != null ? mRouteParameters.toString() : "";
        String queryParameters = mQueryParameters != null ? mQueryParameters.toString() : "";
        return "<Deeplink> URLScheme='" + urlScheme + "' route ='" + route
                + "' routeParameters='" + routeParameters + "' queryParameters='" + queryParameters
                + "'";
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

    public String getRoute() {
        return mRoute;
    }

    public String getSmartlinkClickIdentifier() {
        return mQueryParameters.get(HokoDeeplinkSmartlinkClickIdentifierKey);
    }

    public boolean isSmartlink() {
        return getSmartlinkClickIdentifier() != null;
    }

}
