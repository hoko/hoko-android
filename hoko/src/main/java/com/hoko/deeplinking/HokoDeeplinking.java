package com.hoko.deeplinking;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.hoko.deeplinking.listeners.HokoHandler;
import com.hoko.deeplinking.listeners.HokoLinkGenerationListener;
import com.hoko.model.HokoDeeplink;
import com.hoko.model.exceptions.HokoLinkGenerationException;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * The HokoDeeplinking module provides all the necessary APIs to map, handle and generate deeplinks.
 * Different APIs as provided in order to be as versatile as your application requires them to be.
 */
public class HokoDeeplinking {

    private HokoRouting mRouting;
    private HokoHandling mHandling;
    private HokoLinkGenerator mLinkGenerator;

    public HokoDeeplinking(String token, Context context) {
        mRouting = new HokoRouting(token, context);
        mHandling = new HokoHandling();
        mLinkGenerator = new HokoLinkGenerator(token);
    }

    // Map Routes

    /**
     * Maps a route to an activity class and its fields as route parameters or query parameters.
     *
     * @param route             The route in route format.
     * @param activityClassName The activity's class name.
     * @param routeParameters   The route parameters mapped from Strings to Fields.
     * @param queryParameters   The query parameters mapped from Strings to Fields.
     */
    void mapRoute(String route, String activityClassName, HashMap<String, Field> routeParameters,
                  HashMap<String, Field> queryParameters) {
        mRouting.mapRoute(route, activityClassName, routeParameters, queryParameters);
    }

    /**
     * Maps an activity class as a default route and its fields as query parameters.
     *
     * @param activityClassName The activity's class name.
     * @param queryParameters   The query parameters mapped from Strings to Fields.
     */
    void mapDefaultRoute(String activityClassName, HashMap<String, Field> queryParameters) {
        mapRoute(null, activityClassName, null, queryParameters);
    }

    /**
     * inject(activity) should be called on your DeeplinkRoute activities' onCreate(...) method.
     * It will try to map the current deeplink to annotated DeeplinkRouteParameters or
     * DeeplinkQueryParameters, on that particular activity instance.
     * In case it is not possible to map a deeplink or one does not exist, this function will return
     * false.
     * <pre>{@code
     * Hoko.deeplinking().inject(this);
     * }</pre>
     *
     * @param activity Your activity instance.
     * @return true in case of success, false in case of failure or non-existent deeplink.
     */
    public boolean inject(Activity activity) {
        return mRouting.inject(activity);
    }

    /**
     * inject(fragment) should be called on your DeeplinkRoute fragment' onCreateView(...) method.
     * It will try to map the current deeplink to annotated DeeplinkRouteParameters or
     * DeeplinkQueryParameters, on that particular fragment instance.
     * In case it is not possible to map a deeplink or one does not exist, this function will return
     * false.
     * <pre>{@code
     * Hoko.deeplinking().inject(this);
     * }</pre>
     *
     * @param fragment Your fragment instance.
     * @return true in case of success, false in case of failure or non-existent deeplink.
     */
    public boolean inject(Fragment fragment) {
        return mRouting.inject(fragment);
    }

    /**
     * openURL(urlString) is called when HokoActivity receives a deeplink Intent from the Android
     * OS.
     * It returns true or false depending on whether it has such a deeplink mapped.
     *
     * @param urlString The url passed on the intent.
     * @return true in case of success, false in case of failure or non-existent deeplink.
     */
    public boolean openURL(String urlString) {
        return mRouting.openURL(urlString);
    }

    // Handlers

    /**
     * With addHandler: you can add an object which implements the HokoHandler interface to be
     * called every time your application opens a deeplink. This allows you to track incoming
     * deeplinks outside of the deeplinking targets.
     * <pre>{@code
     * Hoko.deeplinking().addHandler(new HokoHandler() {
     *      public void handle(HokoDeeplink deeplink) {
     *          Log.d("HOKO", deeplink.toString());
     *      }});
     * }</pre>
     *
     * @param handler An object which implements the HokoHandler interface.
     */
    public void addHandler(HokoHandler handler) {
        mHandling.addHandler(handler);
    }

    /**
     * With removeHandler: you can remove a previously added HokoHandler object.
     * <pre>{@code
     * Hoko.deeplinking().removeHandler(analyticsHandler);
     * }</pre>
     *
     * @param handler An object which implements the HokoHandler interface.
     * @return true if the handler was removed, false otherwise.
     */
    public boolean removeHandler(HokoHandler handler) {
        return mHandling.removeHandler(handler);
    }

    // Link Generation

    /**
     * generateHokolink(deeplink, listener) allows the app to generate Hokolinks for the
     * user to share with other users, independent of the platform, users will be redirected to the
     * corresponding view. A user generated HokoDeeplink object may be passed along to generate the
     * deeplinks for all available platforms. In case the request is successful, the onLinkGenerated
     * function will be called receiving an Hokolink (e.g. http://hoko.io/XmPle). Otherwise it will
     * return the cause of failure in the onError function.
     * <pre>{@code
     * HashMap<String, String> routeParameters = new HashMap<String, String>();
     * routeParameters.put("id", "30");
     * Hoko.deeplinking().generateHokolink(HokoDeeplink
     * .deeplink("products/:id", routeParameters, null), new HokoLinkGenerationListener() {
     *      public void onLinkGenerated(String hokolink) {
     *          shareLink(hokolink);
     *      }
     *      public void onError(Exception exception) {
     *          exception.printStackTrace();
     *      }});
     * }</pre>
     *
     * @param deeplink A HKDeeplink object.
     * @param listener A HokoLinkGenerationLister instance.
     */
    public void generateHokolink(HokoDeeplink deeplink, HokoLinkGenerationListener listener) {
        mLinkGenerator.generateHokolink(deeplink, listener);
    }

    /**
     * generateHokolink(activity, listener) allows the app to generate Hoko Hokolinks for the
     * user to share with other users, independent of the platform users will be redirected to the
     * corresponding view. An activity annotated with DeeplinkRoute may be passed along to generate
     * the deeplinks for all available platforms. In case the request is successful, the
     * onLinkGenerated function will be called receiving an hokolink (e.g. http://hoko.io/XmPle).
     * Otherwise it will return the cause of failure in the onError function.
     * <pre>{@code
     * Hoko.deeplinking().generateHokolink(this, new HokoLinkGenerationListener() {
     *      public void onLinkGenerated(String hokolink) {
     *          shareLink(hokolink);
     *      }
     *      public void onError(Exception exception) {
     *          exception.printStackTrace();
     *      }});
     * }</pre>
     *
     * @param activity An activity annotated with DeeplinkRoute.
     * @param listener A HokoLinkGenerationLister instance.
     */
    public void generateHokolink(Activity activity, HokoLinkGenerationListener listener) {
        HokoDeeplink deeplink = HokoAnnotationParser.deeplinkFromActivity(activity);
        if (deeplink != null) {
            generateHokolink(deeplink, listener);
        } else {
            listener.onError(new HokoLinkGenerationException());
        }
    }

    /**
     * generateHokolink(fragment, listener) allows the app to generate Hoko Hokolinks for the
     * user to share with other users, independent of the platform users will be redirected to the
     * corresponding view. An fragment annotated with DeeplinkRoute may be passed along to generate
     * the deeplinks for all available platforms. In case the request is successful, the
     * onLinkGenerated function will be called receiving an hokolink (e.g. http://hoko.io/XmPle).
     * Otherwise it will return the cause of failure in the onError function.
     * <pre>{@code
     * Hoko.deeplinking().generateHokolink(this, new HokoLinkGenerationListener() {
     *      public void onLinkGenerated(String hokolink) {
     *          shareLink(hokolink);
     *      }
     *      public void onError(Exception exception) {
     *          exception.printStackTrace();
     *      }});
     * }</pre>
     *
     * @param fragment A fragment annotated with DeeplinkRoute.
     * @param listener A HokoLinkGenerationLister instance.
     */
    public void generateHokolink(Fragment fragment, HokoLinkGenerationListener listener) {
        HokoDeeplink deeplink = HokoAnnotationParser.deeplinkFromFragment(fragment);
        if (deeplink != null) {
            generateHokolink(deeplink, listener);
        } else {
            listener.onError(new HokoLinkGenerationException());
        }
    }


    public HokoRouting routing() {
        return mRouting;
    }

    HokoHandling handling() {
        return mHandling;
    }

}
