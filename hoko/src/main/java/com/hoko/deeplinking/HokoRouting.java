package com.hoko.deeplinking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.hoko.Hoko;
import com.hoko.model.HokoDeeplink;
import com.hoko.model.HokoRoute;
import com.hoko.model.HokoURL;
import com.hoko.model.exceptions.HokoDuplicateRouteException;
import com.hoko.model.exceptions.HokoMultipleDefaultRoutesException;
import com.hoko.utils.log.HokoLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * HokoRouting contains most of the logic pertaining the mapping or routes and the opening of
 * deeplinks from the HokoDeeplinking module.
 */
public class HokoRouting {

    private ArrayList<HokoRoute> mRoutes;
    private HokoRoute mDefaultRoute;
    private String mToken;
    private Context mContext;

    public HokoRouting(String token, Context context) {
        mToken = token;
        mContext = context;
        mRoutes = new ArrayList<HokoRoute>();
    }

    /**
     * Maps a route with a route format, an activity class name, its route parameter fields and
     * its query parameter fields to a HokoRoute inside HokoRouting.
     *
     * @param route             The route in route format.
     * @param activityClassName The activity class name.
     * @param routeParameters   A HashMap where the keys are the route components and the fields are the values.
     * @param queryParameters   A HashMap where the keys are the query components and the fields are the values.
     */
    public void mapRoute(String route, String activityClassName, HashMap<String, Field> routeParameters, HashMap<String, Field> queryParameters) {
        if (route != null && routeExists(route))
            HokoLog.e(new HokoDuplicateRouteException(route));
        else
            addNewRoute(new HokoRoute(HokoURL.sanitizeURL(route), activityClassName, routeParameters, queryParameters, mContext));
    }

    /**
     * Injects an activity object with the deeplink values from its Intent.
     * This is done by the use of Hoko annotations on the class and on its fields.
     *
     * @param activity An annotated activity object.
     * @return true if it could inject the values, false otherwise.
     */
    public boolean inject(Activity activity) {
        return HokoAnnotationParser.inject(activity);
    }

    /**
     * Injects an fragment object with the deeplink values from its Bundle.
     * This is done by the use of Hoko annotations on the class and on its fields.
     *
     * @param fragment An annotated fragment object.
     * @return true if it could inject the values, false otherwise.
     */
    public boolean inject(Fragment fragment) {
        return HokoAnnotationParser.inject(fragment);
    }

    /**
     * Returns a mapped route for a given routeString. Falls back to the default route if possible,
     * returns null otherwise.
     *
     * @param routeString A route format string.
     * @return A HokoRoute object or null.
     */
    public HokoRoute getRoute(String routeString) {
        if (routeString == null) {
            if (mDefaultRoute != null)
                return mDefaultRoute;
            else
                return null;
        }
        for (HokoRoute route : mRoutes) {
            if (routeString.equalsIgnoreCase(route.getRoute()))
                return route;
        }
        return null;
    }

    /**
     * Open URL serves the purpose of opening a deeplink from within the HokoActivity.
     * This function will redirect the deeplink to a given DeeplinkRoute or DeeplinkFragment
     * Activity in case it finds a match. Falls back to the default route or doesn't do anything
     * if a default route does not exist.
     *
     * @param urlString The deeplink.
     * @return true if it can open the deeplink, false otherwise.
     */
    public boolean openURL(String urlString) {
        HokoURL url = new HokoURL(urlString);
        return handleOpenURL(url);
    }

    /**
     * Tries to get an intent for a given deeplink, in case it can't, returns false.
     * If it gets an intent it will open the intent, starting a given activity.
     *
     * @param url A HokoURL object.
     * @return true in case in opened the activity, false otherwise.
     */
    private boolean handleOpenURL(HokoURL url) {
        Intent intent = intentForURL(url);
        if (intent != null) {
            openIntent(intent);
            return true;
        }
        return false;
    }

    /**
     * This function maps a HokoURL object to an Intent through the use of the mapped HokoRoutes.
     * Will fallback to the default route if possible, or will return null in case it does not
     * exist.
     *
     * @param url A HokoURL object.
     * @return true in case the Intent was created, false otherwise.
     */
    public Intent intentForURL(HokoURL url) {
        for (HokoRoute route : mRoutes) {
            HashMap<String, String> routeParameters = url.matchesWithRoute(route);
            if (routeParameters != null) {
                HokoDeeplink deeplink = new HokoDeeplink(url.getScheme(), route.getRoute(), routeParameters, url.getQueryParameters());
                Hoko.deeplinking().handling().handle(deeplink);
                return route.getIntent(url);
            }
        }

        if (mDefaultRoute != null) {
            HokoDeeplink deeplink = new HokoDeeplink(url.getScheme(), null, null, url.getQueryParameters());
            Hoko.deeplinking().handling().handle(deeplink);
            return mDefaultRoute.getIntent(url);
        }
        return null;
    }

    /**
     * Utility function to start an activity with a given intent.
     *
     * @param intent The intent to start the activity.
     */
    private void openIntent(Intent intent) {
        mContext.startActivity(intent);
    }

    /**
     * Function to add a new HokoRoute to the routes list (or as a default route).
     * It also checks if the route is valid or not.
     *
     * @param route A HokoRoute object.
     */
    private void addNewRoute(HokoRoute route) {
        if (route.getRoute() == null || route.getRoute().length() == 0) {
            if (mDefaultRoute == null) {
                mDefaultRoute = route;
            } else {
                HokoLog.e(new HokoMultipleDefaultRoutesException(route.getActivityClassName()));
            }
        } else {
            if (route.isValid()) {
                mRoutes.add(route);
                if (Hoko.isDebugMode())
                    route.post(mToken);
            }
        }
    }

    /**
     * Checks if a given route already already exists in the HokoRouting routes list.
     * Also checks if a default route was already assigned.
     *
     * @param route A route in route format.
     * @return true if it exists, false otherwise.
     */
    public boolean routeExists(String route) {
        if (route == null) {
            return mDefaultRoute != null;
        }
        for (HokoRoute routeObj : mRoutes) {
            if (routeObj.getRoute().compareToIgnoreCase(HokoURL.sanitizeURL(route)) == 0)
                return true;
        }
        return false;
    }
}
