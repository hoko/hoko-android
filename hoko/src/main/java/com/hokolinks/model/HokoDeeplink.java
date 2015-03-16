package com.hokolinks.model;

import android.content.Context;

import com.hokolinks.utils.HokoDateUtils;
import com.hokolinks.utils.HokoUtils;
import com.hokolinks.utils.networking.HokoNetworking;
import com.hokolinks.utils.networking.async.HokoHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * HokoDeeplink is the model which represents an inbound or outbound deeplink object.
 * It contains a route format string, the route parameters, the query parameters and an optional
 * url scheme.
 */
public class HokoDeeplink {

    // Key values from incoming deeplinks
    public static final String HokoDeeplinkLinkIdentifierKey = "hk_id";
    //TODO change to hokolink
    public static final String HokoDeeplinkHokolinkIdentifierKey = "hk_oid";

    private static final int HokoDeeplinkOpenedStatus = 3;

    private String mRoute;
    private HashMap<String, String> mRouteParameters;
    private HashMap<String, String> mQueryParameters;
    private String mURLScheme;

    /**
     * The constructor for HokoDeeplink objects.
     *
     * @param urlScheme       Optional url scheme.
     * @param route           A route in route format.
     * @param routeParameters A HashMap where the keys are the route components and the values are
     *                        the route parameters.
     * @param queryParameters A HashMap where the keys are the query components and the values are
     *                        the query parameters.
     */
    public HokoDeeplink(String urlScheme, String route, HashMap<String, String> routeParameters,
                        HashMap<String, String> queryParameters) {
        if (urlScheme == null)
            mURLScheme = "";
        else
            mURLScheme = urlScheme;

        mRoute = route;
        mRouteParameters = routeParameters;
        mQueryParameters = queryParameters;
    }

    /**
     * An easy to use static function for the developer to generate their own deeplinks to
     * generate Hokolinks afterwards.
     *
     * @param route           A route in route format.
     * @param routeParameters A HashMap where the keys are the route components and the values are
     *                        the route parameters.
     * @param queryParameters A HashMap where the keys are the query components and the values are
     *                        the query parameters.
     * @return The generated HokoDeeplink.
     */
    public static HokoDeeplink deeplink(String route, HashMap<String, String> routeParameters,
                                        HashMap<String, String> queryParameters) {
        HokoDeeplink deeplink = new HokoDeeplink(null, HokoUtils.sanitizeRoute(route),
                routeParameters, queryParameters);

        if (HokoDeeplink.matchRoute(deeplink.getRoute(), deeplink.getRouteParameters())) {
            return deeplink;
        }
        return null;
    }

    /**
     * This function serves the purpose of communicating to the Hoko backend service that a given
     * inbound deeplink object was opened either through the notification id or through the
     * deeplink id.
     *
     * @param context A context object.
     * @param token   The Hoko API Token.
     * @param user    A user object.
     */
    public void post(Context context, String token, HokoUser user) {
        //TODO Change to hokolink
        if (isHokolink()) {
            HokoNetworking.getNetworking().addRequest(
                    new HokoHttpRequest(HokoHttpRequest.HokoNetworkOperationType.POST,
                            "omnilinks/" + getHokolinkIdentifier() + "/open", token,
                            hokolinkJSON(context, user).toString()));
        }

    }

    /**
     * Converts all the HokoDeeplink information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @return The JSONObject representation of HokoDeeplink.
     */
    public JSONObject json() {
        try {
            JSONObject root = new JSONObject();
            JSONObject route = new JSONObject();
            route.putOpt("format", mRoute);
            if (mRouteParameters != null)
                route.putOpt("parameters", new JSONObject(mRouteParameters));
            if (mQueryParameters != null)
                route.putOpt("query", new JSONObject(mQueryParameters));
            root.put("route", route);
            return root;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Converts the HokoDeeplink into a JSONObject referring the notification that was opened.
     *
     * @return The JSONObject representation of the notification.
     */
    private JSONObject notificationJSON() {
        try {
            JSONObject root = new JSONObject();
            JSONObject notification = new JSONObject();
            notification.put("opened_at", HokoDateUtils.format(new Date()));
            notification.put("status", HokoDeeplinkOpenedStatus);
            root.put("notification", notification);
            return root;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Converts the HokoDeeplink into a JSONObject referring the Hokolink that was opened.
     *
     * @param context A context object.
     * @param user    A user object.
     * @return The JSONObject representation of the Hokolink.
     */
    private JSONObject hokolinkJSON(Context context, HokoUser user) {
        //TODO change to hokolink
        try {
            JSONObject root = new JSONObject();
            JSONObject omnilink = new JSONObject();
            omnilink.putOpt("omnilink_open_id", getLinkIdentifier());
            omnilink.put("opened_at", HokoDateUtils.format(new Date()));
            omnilink.put("user", user.baseJSON(context));
            root.put("omnilink", omnilink);
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
        return "<HokoDeeplink> URLScheme='" + urlScheme + "' route ='" + route
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

    public String getHokolinkIdentifier() {
        return mQueryParameters.get(HokoDeeplinkHokolinkIdentifierKey);
    }

    public String getLinkIdentifier() {
        return mQueryParameters.get(HokoDeeplinkLinkIdentifierKey);
    }

    public boolean isHokolink() {
        return getHokolinkIdentifier() != null;
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

}
