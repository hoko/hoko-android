package com.hokolinks.model;


import android.content.Context;

import com.hokolinks.utils.Utils;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.networking.async.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Route {
    String mRoute;

    public Route(String route) {
        mRoute = route;
    }

    public String getRoute() {
        return mRoute;
    }

    /**
     * Splits the route format into components.
     *
     * @return A List of Route components.
     */
    public List<String> getComponents() {
        if (mRoute == null)
            return null;
        return new ArrayList<>(Arrays.asList(mRoute.split("/")));
    }

    public abstract void execute(URL url);

    /**
     * This function serves the purpose of communicating to the Hoko backend service that a given
     * route is available on this application.
     *
     * @param token The Hoko API Token.
     */
    public void post(String token, Context context) {
        if (!hasBeenPosted(context)) {
            Utils.saveBoolean(true, mRoute, context);
            Networking.getNetworking().addRequest(
                    new HttpRequest(HttpRequest.HokoNetworkOperationType.POST, "routes", token,
                            getJSON(context).toString()));
        }
    }

    /**
     * Converts all the Route information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @return The JSONObject representation of Route.
     */
    public JSONObject getJSON(Context context) {
        try {
            JSONObject root = new JSONObject();
            JSONObject route = new JSONObject();
            route.put("build", App.getVersionCode(context));
            route.put("device", Device.getVendor() + " " + Device.getModel());
            route.put("path", mRoute);
            route.put("version", App.getVersion(context));
            root.put("route", route);
            return root;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private boolean hasBeenPosted(Context context) {
        return Utils.getBoolean(mRoute, context);
    }

}
