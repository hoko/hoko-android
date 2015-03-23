package com.hokolinks.deeplinking;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.hokolinks.deeplinking.listeners.Handler;
import com.hokolinks.deeplinking.listeners.LinkGenerationListener;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.exceptions.LinkGenerationException;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * The Deeplinking module provides all the necessary APIs to map, handle and generate deeplinks.
 * Different APIs as provided in order to be as versatile as your application requires them to be.
 */
public class Deeplinking {

    private Routing mRouting;
    private Handling mHandling;
    private LinkGenerator mLinkGenerator;

    public Deeplinking(String token, Context context) {
        mRouting = new Routing(token, context);
        mHandling = new Handling();
        mLinkGenerator = new LinkGenerator(token);
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
     * With addHandler: you can add an object which implements the Handler interface to be
     * called every time your application opens a deeplink. This allows you to track incoming
     * deeplinks outside of the deeplinking targets.
     * <pre>{@code
     * Hoko.deeplinking().addHandler(new Handler() {
     *      public void handle(Deeplink deeplink) {
     *          Log.d("HOKO", deeplink.toString());
     *      }});
     * }</pre>
     *
     * @param handler An object which implements the Handler interface.
     */
    public void addHandler(Handler handler) {
        mHandling.addHandler(handler);
    }

    /**
     * With removeHandler: you can remove a previously added Handler object.
     * <pre>{@code
     * Hoko.deeplinking().removeHandler(analyticsHandler);
     * }</pre>
     *
     * @param handler An object which implements the Handler interface.
     * @return true if the handler was removed, false otherwise.
     */
    public boolean removeHandler(Handler handler) {
        return mHandling.removeHandler(handler);
    }

    // Link Generation

    /**
     * generateSmartlink(deeplink, listener) allows the app to generate Smartlinks for the
     * user to share with other users, independent of the platform, users will be redirected to the
     * corresponding view. A user generated Deeplink object may be passed along to generate the
     * deeplinks for all available platforms. In case the request is successful, the onLinkGenerated
     * function will be called receiving an Smartlink (e.g. http://hoko.io/XmPle). Otherwise it will
     * return the cause of failure in the onError function.
     * <pre>{@code
     * HashMap<String, String> routeParameters = new HashMap<String, String>();
     * routeParameters.put("id", "30");
     * Hoko.deeplinking().generateSmartlink(Deeplink
     * .deeplink("products/:id", routeParameters, null), new LinkGenerationListener() {
     *      public void onLinkGenerated(String smartlink) {
     *          shareLink(smartlink);
     *      }
     *      public void onError(Exception exception) {
     *          exception.printStackTrace();
     *      }});
     * }</pre>
     *
     * @param deeplink A HKDeeplink object.
     * @param listener A HokoLinkGenerationLister instance.
     */
    public void generateSmartlink(Deeplink deeplink, LinkGenerationListener listener) {
        mLinkGenerator.generateHokolink(deeplink, listener);
    }

    /**
     * generateSmartlink(activity, listener) allows the app to generate Hoko Smartlinks for the
     * user to share with other users, independent of the platform users will be redirected to the
     * corresponding view. An activity annotated with DeeplinkRoute may be passed along to generate
     * the deeplinks for all available platforms. In case the request is successful, the
     * onLinkGenerated function will be called receiving an smartlink (e.g. http://hoko.io/XmPle).
     * Otherwise it will return the cause of failure in the onError function.
     * <pre>{@code
     * Hoko.deeplinking().generateSmartlink(this, new LinkGenerationListener() {
     *      public void onLinkGenerated(String smartlink) {
     *          shareLink(smartlink);
     *      }
     *      public void onError(Exception exception) {
     *          exception.printStackTrace();
     *      }});
     * }</pre>
     *
     * @param activity An activity annotated with DeeplinkRoute.
     * @param listener A HokoLinkGenerationLister instance.
     */
    public void generateSmartlink(Activity activity, LinkGenerationListener listener) {
        Deeplink deeplink = AnnotationParser.deeplinkFromActivity(activity);
        if (deeplink != null) {
            generateSmartlink(deeplink, listener);
        } else {
            listener.onError(new LinkGenerationException());
        }
    }

    /**
     * generateSmartlink(fragment, listener) allows the app to generate Smartlinks for the
     * user to share with other users, independent of the platform users will be redirected to the
     * corresponding view. An fragment annotated with DeeplinkRoute may be passed along to generate
     * the deeplinks for all available platforms. In case the request is successful, the
     * onLinkGenerated function will be called receiving an smartlink (e.g. http://hoko.io/XmPle).
     * Otherwise it will return the cause of failure in the onError function.
     * <pre>{@code
     * Hoko.deeplinking().generateSmartlink(this, new LinkGenerationListener() {
     *      public void onLinkGenerated(String smartlink) {
     *          shareLink(smartlink);
     *      }
     *      public void onError(Exception exception) {
     *          exception.printStackTrace();
     *      }});
     * }</pre>
     *
     * @param fragment A fragment annotated with DeeplinkRoute.
     * @param listener A HokoLinkGenerationLister instance.
     */
    public void generateSmartlink(Fragment fragment, LinkGenerationListener listener) {
        Deeplink deeplink = AnnotationParser.deeplinkFromFragment(fragment);
        if (deeplink != null) {
            generateSmartlink(deeplink, listener);
        } else {
            listener.onError(new LinkGenerationException());
        }
    }


    public Routing routing() {
        return mRouting;
    }

    Handling handling() {
        return mHandling;
    }

}
