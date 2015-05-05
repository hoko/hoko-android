package com.hokolinks.deeplinking;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.hokolinks.Hoko;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.DeeplinkCallback;
import com.hokolinks.model.IntentRouteImpl;
import com.hokolinks.model.Route;
import com.hokolinks.model.RouteImpl;
import com.hokolinks.model.URL;
import com.hokolinks.model.exceptions.DuplicateRouteException;
import com.hokolinks.model.exceptions.MultipleDefaultRoutesException;
import com.hokolinks.utils.log.HokoLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Routing contains most of the logic pertaining the mapping or routes and the opening of
 * deeplinks from the Deeplinking module.
 */
public class Routing {

    private ArrayList<Route> mRoutes;
    private Route mDefaultRoute;
    private String mToken;
    private Context mContext;
    private Handling mHandling;

    public Routing(String token, Context context, Handling handling) {
        mToken = token;
        mContext = context;
        mHandling = handling;
        mRoutes = new ArrayList<>();
    }

    /**
     * Maps a route with a route format to a callback.
     *
     * @param route    The route in route format.
     * @param callback A DeeplinkCallback object
     */
    public void mapRoute(String route, DeeplinkCallback callback) {
        if (route != null && routeExists(route))
            HokoLog.e(new DuplicateRouteException(route));
        else
            addNewRoute(new RouteImpl(URL.sanitizeURL(route), callback));
    }

    /**
     * Maps a route with a route format, an activity class name, its route parameter fields and
     * its query parameter fields to a Route inside Routing.
     *
     * @param route             The route in route format.
     * @param activityClassName The activity class name.
     * @param routeParameters   A HashMap where the keys are the route components and the fields are
     *                          the values.
     * @param queryParameters   A HashMap where the keys are the query components and the fields are
     *                          the values.
     */
    public void mapRoute(String route, String activityClassName,
                         HashMap<String, Field> routeParameters,
                         HashMap<String, Field> queryParameters) {
        if (route != null && routeExists(route))
            HokoLog.e(new DuplicateRouteException(route));
        else
            addNewRoute(new IntentRouteImpl(URL.sanitizeURL(route), activityClassName,
                    routeParameters, queryParameters, mContext));
    }

    /**
     * Injects an activity object with the deeplink values from its Intent.
     * This is done by the use of Hoko annotations on the class and on its fields.
     *
     * @param activity An annotated activity object.
     * @return true if it could inject the values, false otherwise.
     */
    public boolean inject(Activity activity) {
        return AnnotationParser.inject(activity);
    }

    /**
     * Injects an fragment object with the deeplink values from its Bundle.
     * This is done by the use of Hoko annotations on the class and on its fields.
     *
     * @param fragment An annotated fragment object.
     * @return true if it could inject the values, false otherwise.
     */
    public boolean inject(android.app.Fragment fragment) {
        return AnnotationParser.inject(fragment);
    }

    /**
     * Injects an fragment object with the deeplink values from its Bundle.
     * This is done by the use of Hoko annotations on the class and on its fields.
     *
     * @param fragment An annotated fragment object.
     * @return true if it could inject the values, false otherwise.
     */
    public boolean inject(Fragment fragment) {
        return AnnotationParser.inject(fragment);
    }

    /**
     * Returns a mapped route for a given routeString. Falls back to the default route if possible,
     * returns null otherwise.
     *
     * @param routeString A route format string.
     * @return A Route object or null.
     */
    public Route getRoute(String routeString) {
        if (routeString == null) {
            if (mDefaultRoute != null)
                return mDefaultRoute;
            else
                return null;
        }
        for (Route route : mRoutes) {
            if (routeString.equalsIgnoreCase(route.getRoute()))
                return route;
        }
        return null;
    }

    /**
     * Open URL serves the purpose of opening a deeplink from within the HokoActivity.
     * This function will redirect the deeplink to a given DeeplinkRoute or
     * DeeplinkFragmentActivity Activity in case it finds a match. Falls back to the default route
     * or doesn't do anything if a default route does not exist.
     *
     * @param urlString The deeplink.
     * @return true if it can open the deeplink, false otherwise.
     */
    public boolean openURL(String urlString) {
        HokoLog.d("Opening Deeplink " + urlString);
        URL url = new URL(urlString);
        return handleOpenURL(url);
    }

    /**
     * Tries to get an intent for a given deeplink, in case it can't, returns false.
     * If it gets an intent it will open the intent, starting a given activity.
     *
     * @param url A URL object.
     * @return true in case in opened the activity, false otherwise.
     */
    private boolean handleOpenURL(URL url) {
        Route route = routeForURL(url);
        if (route != null) {
            route.execute(url);
            notifyHandler(route, url);
            return true;
        }
        return false;
    }

    /**
     * This function will try to find a Route object matching the deeplink found.
     *
     * @param url A URL object.
     * @return Route found
     */
    public Route routeForURL(URL url) {
        for (Route route : mRoutes) {
            if (url.matchesWithRoute(route) != null) {
                return route;
            }
        }

        if (mDefaultRoute != null) {
            return mDefaultRoute;
        }
        return null;
    }

    private void notifyHandler(Route route, URL url) {
        if (route == mDefaultRoute) {
            Deeplink deeplink = new Deeplink(url.getScheme(), null, null,
                    url.getQueryParameters());
            mHandling.handle(deeplink);
        } else {
            HashMap<String, String> routeParameters = url.matchesWithRoute(route);
            if (routeParameters != null) {
                Deeplink deeplink = new Deeplink(url.getScheme(), route.getRoute(),
                        routeParameters, url.getQueryParameters());
                mHandling.handle(deeplink);
            }
        }
    }


    /**
     * Function to add a new Route to the routes list (or as a default route).
     * It also checks if the route is valid or not.
     *
     * @param route A Route object.
     */
    private void addNewRoute(RouteImpl route) {
        if (route.getRoute() == null || route.getRoute().length() == 0) {
            if (mDefaultRoute == null) {
                mDefaultRoute = route;
            } else {
                HokoLog.e(new MultipleDefaultRoutesException(route.getClass().getCanonicalName()));
            }
        } else {
            mRoutes.add(route);
            if (Hoko.isDebugMode())
                route.post(mToken, mContext);

        }
    }

    /**
     * Function to add a new Route to the routes list (or as a default route).
     * It also checks if the route is valid or not.
     *
     * @param intentRoute A Route object.
     */
    private void addNewRoute(IntentRouteImpl intentRoute) {
        if (intentRoute.getRoute() == null || intentRoute.getRoute().length() == 0) {
            if (mDefaultRoute == null) {
                mDefaultRoute = intentRoute;
            } else {
                HokoLog.e(new MultipleDefaultRoutesException(intentRoute.getActivityClassName()));
            }
        } else {
            if (intentRoute.isValid()) {
                mRoutes.add(intentRoute);
                if (Hoko.isDebugMode())
                    intentRoute.post(mToken, mContext);
            }
        }
    }

    /**
     * Checks if a given route already already exists in the Routing routes list.
     * Also checks if a default route was already assigned.
     *
     * @param route A route in route format.
     * @return true if it exists, false otherwise.
     */
    public boolean routeExists(String route) {
        if (route == null) {
            return mDefaultRoute != null;
        }
        for (Route routeObj : mRoutes) {
            if (routeObj.getRoute().compareToIgnoreCase(URL.sanitizeURL(route)) == 0)
                return true;
        }
        return false;
    }
}
