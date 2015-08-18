package com.hokolinks.deeplinking;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.hokolinks.deeplinking.listeners.LinkGenerationListener;
import com.hokolinks.deeplinking.listeners.SmartlinkResolveListener;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.DeeplinkCallback;
import com.hokolinks.model.FilterCallback;
import com.hokolinks.model.exceptions.LinkGenerationException;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.networking.async.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * The Deeplinking module provides all the necessary APIs to map, handle and generate deeplinks.
 * Different APIs as provided in order to be as versatile as your application requires them to be.
 */
public class Deeplinking {

    private static final String INSTALL_PATH = "installs/android";
    private Routing mRouting;
    private Handling mHandling;
    private Filtering mFiltering;
    private LinkGenerator mLinkGenerator;
    private Resolver mResolver;
    private String mToken;

    public Deeplinking(String token, Context context) {
        mToken = token;
        mHandling = new Handling();
        mFiltering = new Filtering();
        mRouting = new Routing(token, context, mHandling, mFiltering);
        mLinkGenerator = new LinkGenerator(token);
        mResolver = new Resolver(token, context);
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
     * Maps a route with a specific callback that will be executed whenever the mapped deep link
     * is opened.
     *
     * @param route             The route in route format.
     * @param callback          The callback that will be executed when the mapped deep link is opened.
     */
    public void mapRoute(String route, DeeplinkCallback callback) {
        mRouting.mapRoute(route, callback);
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
     * Maps the default route with a specific callback. Only deep links that do not match any
     * existing routes will trigger the default route.
     *
     * @param callback          The callback that will be executed when the opened deep link does not match
     *                          any existing routes.
     */
    public void mapDefaultRoute(DeeplinkCallback callback) {
        mapRoute(null, callback);
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
     * inject(fragment) should be called on your DeeplinkRoute fragment' onCreateView(...)
     * method. It will try to map the current deeplink to annotated DeeplinkRouteParameters or
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
    public boolean inject(android.app.Fragment fragment) {
        return mRouting.inject(fragment);
    }

    /**
     * inject(fragment) should be called on your DeeplinkRoute fragment' onCreateView(...)
     * method. It will try to map the current deeplink to annotated DeeplinkRouteParameters or
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
        return openURL(urlString, null);
    }

    /**
     * openURL(urlString) is called when HokoActivity receives a deeplink Intent from the Android
     * OS.
     * It returns true or false depending on whether it has such a deeplink mapped.
     *
     * @param urlString The url passed on the intent.
     * @param metadata The metadata in JSON format which was passed when the smartlink was created.
     * @return true in case of success, false in case of failure or non-existent deeplink.
     */
    public boolean openURL(String urlString, JSONObject metadata) {
        return mRouting.openURL(urlString, metadata);
    }

    /**
     * openDeferredURL(urlString) is called when DeferredDeeplinkingBroadcastReceiver receives a
     * deeplink Intent from Google Play.
     *
     * @param urlString The url passed on the intent.
     */
    void openDeferredURL(String urlString) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deeplink", urlString);
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        Networking.getNetworking().addRequest(
                new HttpRequest(HttpRequest.HokoNetworkOperationType.POST,
                        HttpRequest.getURLFromPath(INSTALL_PATH), mToken, jsonObject.toString()));
        mRouting.openURL(urlString, null);
    }

    /**
     * openSmartlink(smartlink) should be called when a Smartlink needs to be resolved into a
     * deeplink to open the correct view. e.g. Opening a Smartlink from a push notification.
     *
     * @param smartlink A smartlink string.
     */
    public void openSmartlink(String smartlink) {
        openSmartlink(smartlink, null);
    }

    /**
     * openSmartlink(smartlink) should be called when a Smartlink needs to be resolved into a
     * deeplink to open the correct view. e.g. Opening a Smartlink from a push notification.
     *
     * @param smartlink                A smartlink string.
     * @param smartlinkResolveListener A link resolved listener for lifecycle purposes, called
     *                                 before opening the deeplink.
     */
    public void openSmartlink(String smartlink, final SmartlinkResolveListener smartlinkResolveListener) {
        mResolver.resolveSmartlink(smartlink, new SmartlinkResolveListener() {
            @Override
            public void onLinkResolved(String deeplink, JSONObject metadata) {
                if (smartlinkResolveListener != null) {
                    smartlinkResolveListener.onLinkResolved(deeplink, metadata);
                }
                openURL(deeplink, metadata);
            }

            @Override
            public void onError(Exception e) {
                if (smartlinkResolveListener != null) {
                    smartlinkResolveListener.onError(e);
                }
            }
        });
    }

    /**
     * This method will return the current the last deep link that was processed (whether it was
     * sucessfully opened, or not, due to filters) by the HOKO SDK. If no deep links were processed
     * at the time of the call, this will return null.
     *
     * @return The last deeplink object that was processed by the SDK.
     */
    public Deeplink getCurrentDeeplink() {
        return mRouting.getCurrentDeeplink();
    }

    /**
     * This method will try to open the last deep link that was processed (whether it was
     * sucessfully opened, or not, due to filters) by calling the route that is currently mapping
     * this deeplink and the handlers.
     * If the deeplink object is not nil and was opened during this call, the method will return
     * true, otherwise false.
     *
     * @return Returns true if the current deeplink object was sucessfully opened, false otherwise.
     */
    public boolean openCurrentDeeplink() {
        return mRouting.openCurrentDeeplink();
    }

    /**
     * This method will try to open the deeplink object given in the parameters,
     * by calling the route that is currently mapping that deeplink and the handlers.
     * If the deeplink was opened during this call, the method will return true, otherwise false
     *
     * @param deeplink The deeplink object that will be opened
     * @return true if the deeplink object given in the parameters was successfully opened, false
     * otherwise.
     */
    public boolean openDeeplink(Deeplink deeplink) {
        return mRouting.openDeeplink(deeplink);
    }

    // Handlers

    /**
     * With addHandler() you can add an object which implements the DeeplinkCallback interface to be
     * called every time your application opens a deeplink. This allows you to track incoming
     * deeplinks outside of the deeplinking targets.
     * <pre>{@code
     * Hoko.deeplinking().addHandler(new DeeplinkCallback() {
     *      public void deeplinkOpened(Deeplink deeplink) {
     *          Log.d("HOKO", deeplink.toString());
     *      }});
     * }</pre>
     *
     * @param callback An object which implements the DeeplinkCallback interface.
     */
    public void addHandler(DeeplinkCallback callback) {
        mHandling.addHandler(callback);
    }

    /**
     * With removeHandler() you can remove a previously added DeeplinkCallback object.
     * <pre>{@code
     * Hoko.deeplinking().removeHandler(analyticsHandler);
     * }</pre>
     *
     * @param callback An object which implements the DeeplinkCallback interface.
     * @return true if the handler was removed, false otherwise.
     */
    public boolean removeHandler(DeeplinkCallback callback) {
        return mHandling.removeHandler(callback);
    }

    // Filters

    /**
     * With addFilter() you can add an object which implements the FilterCallback interface to be
     * called every time your application opens a deeplink. This allows to filter out deeplinks on
     * your application.
     * <pre>{@code
     * Hoko.deeplinking().addFilter(new FilterCallback() {
     *      public boolean openDeeplink(Deeplink deeplink) {
     *          return user.isLoggedIn();
     *      }});
     * }</pre>
     *
     * @param filterCallback An object which implements the FilterCallback interface.
     */
    public void addFilter(FilterCallback filterCallback) {
        mFiltering.addFilter(filterCallback);
    }

    /**
     * With removeFilter() you can remove a previously added FilterCallback object.
     * <pre>{@code
     * Hoko.deeplinking().removeFilter(userLoggedInFilter);
     * }</pre>
     *
     * @param filterCallback An object which implements the DeeplinkCallback interface.
     * @return true if the handler was removed, false otherwise.
     */
    public boolean removeFilter(FilterCallback filterCallback) {
        return mFiltering.removeFilter(filterCallback);
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
        mLinkGenerator.generateSmartlink(deeplink, listener);
    }

    /**
     * generateSmartlink(activity, listener) allows the app to generate Hoko Smartlinks for the
     * user to share with other users, independent of the platform users will be redirected to the
     * corresponding view. An activity annotated with DeeplinkRoute may be passed along to
     * generate the deeplinks for all available platforms. In case the request is successful, the
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
     * corresponding view. An fragment annotated with DeeplinkRoute may be passed along to
     * generate the deeplinks for all available platforms. In case the request is successful, the
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

    /**
     * generateSmartlink(fragment, listener) allows the app to generate Smartlinks for the
     * user to share with other users, independent of the platform users will be redirected to the
     * corresponding view. An fragment annotated with DeeplinkRoute may be passed along to
     * generate the deeplinks for all available platforms. In case the request is successful, the
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
    public void generateSmartlink(android.app.Fragment fragment, LinkGenerationListener listener) {
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
