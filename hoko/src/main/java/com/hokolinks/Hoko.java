package com.hokolinks;

import android.content.Context;

import com.hokolinks.analytics.Analytics;
import com.hokolinks.deeplinking.AnnotationParser;
import com.hokolinks.deeplinking.Deeplinking;
import com.hokolinks.model.App;
import com.hokolinks.model.exceptions.SetupCalledMoreThanOnceException;
import com.hokolinks.model.exceptions.SetupNotCalledYetException;
import com.hokolinks.utils.log.Log;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.versionchecker.VersionChecker;

/**
 * Hoko is an easy-to-use Framework to handle Deeplinking and the Analytics around it.
 * <p/>
 * This is a simple drop-in class for handling incoming deeplinks.
 * With the Hoko framework you can map routes to your activities, add handlers that trigger when
 * deeplinks are the point of entry to your application.
 * <p/>
 * Hoko includes three separate modules:
 * - Deeplinking - handles every incoming deeplink, so long as it has been mapped
 * - Analytics - handles the tracking of users and events to allow creation and evaluation of
 * campaigns
 * <p/>
 * You should setup Hoko on your Application's onCreate(...), by calling
 * Hoko.setup(this, "YOUR-API-TOKEN")
 */
public class Hoko {

    public static final String VERSION = "1.1.4.2";

    // Static Instance
    private static Hoko mInstance;

    // Private modules
    private Deeplinking mDeeplinking;
    private Analytics mAnalytics;

    // Private variables
    private boolean mDebugMode;

    // Private initializer
    private Hoko(Context context, String token, boolean debugMode) {
        mDebugMode = debugMode;

        Networking.setupNetworking(context);

        mDeeplinking = new Deeplinking(token, context);
        mAnalytics = new Analytics(token, context);
        mDeeplinking.addHandler(mAnalytics);
    }

    // Setup

    /**
     * Setups all the Hoko module instances, logging and asynchronous networking queues.
     * Setting up with a token will make sure you can take full advantage of the Hoko service,
     * as you will be able to track everything through manual or automatic Analytics, which will
     * be shown on your Hoko dashboards.
     * This will also trigger the debug mode if you are running with a BuildConfig.DEBUG = true.
     * If you want to force the debug mode manually use the setup(context, token, debugMode)
     * call.
     * <pre>{@code
     * Hoko.setup(this, "YOUR-API-TOKEN");
     * }</pre>
     *
     * @param context Your application context.
     * @param token   Hoko service API key.
     */
    public static void setup(Context context, String token) {
        setup(context, token, App.isDebug(context));
    }

    /**
     * Setups all the Hoko module instances, logging and asynchronous networking queues.
     * Setting up with a token will make sure you can take full advantage of the Hoko service,
     * as you will be able to track everything through manual or automatic Analytics, which will
     * be shown on your Hoko dashboards.
     * Also sets the debug mode manually. Debug mode serves the purpose of uploading the app icon
     * and the mapped HokoRoutes to the Hoko backend service.
     * <pre>{@code
     * Hoko.setup(this, "YOUR-API-TOKEN", true);
     * }</pre>
     *
     * @param context   Your application context.
     * @param token     Hoko service API key.
     * @param debugMode Toggle debug mode manually.
     */
    public static void setup(Context context, String token, boolean debugMode) {
        if (mInstance == null) {
            mInstance = new Hoko(context, token, debugMode);
            AnnotationParser.parseActivities(context);
            if (debugMode) {
                App.postIcon(token, context);
                VersionChecker.getInstance().checkForNewVersion(VERSION);
            }
        } else {
            Log.e(new SetupCalledMoreThanOnceException());
        }
    }

    // Modules

    /**
     * The Deeplinking module provides all the necessary APIs to map, handle and generate
     * deeplinks.
     * Different APIs as provided in order to be as versatile as your application requires them to
     * be.
     *
     * @return A reference to the Deeplinking instance.
     */
    public static Deeplinking deeplinking() {
        if (mInstance == null) {
            Log.e(new SetupNotCalledYetException());
            return null;
        }
        return mInstance.mDeeplinking;
    }

    /**
     * The Analytics module provides all the necessary APIs to manage user and application
     * behavior.
     * Users should be identified to this module, as well as key events (e.g. sales, referrals, etc)
     * in order to track campaign value and allow user segmentation.
     *
     * @return A reference to the Analytics instance.
     */
    public static Analytics analytics() {
        if (mInstance == null) {
            Log.e(new SetupNotCalledYetException());
            return null;
        }
        return mInstance.mAnalytics;
    }


    // Logging

    /**
     * Use this function to enable or disable logging from the Hoko SDK.
     * It is disabled by default.
     *
     * @param verbose true to enable logging, false to disable.
     */
    public static void setVerbose(boolean verbose) {
        Log.setVerbose(verbose);
    }

    // Debug

    /**
     * Returns a boolean on whether the debug mode is activated or not.
     *
     * @return true if debug mode is on, false otherwise.
     */
    public static boolean isDebugMode() {
        if (mInstance == null) {
            Log.e(new SetupNotCalledYetException());
            return false;
        }
        return mInstance.mDebugMode;
    }

}
