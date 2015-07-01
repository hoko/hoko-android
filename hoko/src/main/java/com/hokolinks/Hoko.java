package com.hokolinks;

import android.content.Context;
import android.util.Log;

import com.hokolinks.deeplinking.AnnotationParser;
import com.hokolinks.deeplinking.Deeplinking;
import com.hokolinks.model.Device;
import com.hokolinks.model.exceptions.SetupCalledMoreThanOnceException;
import com.hokolinks.model.exceptions.SetupNotCalledYetException;
import com.hokolinks.utils.Utils;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.versionchecker.VersionChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hoko is an easy-to-use Framework to handle Deeplinking and the Analytics around it.
 *
 * This is a simple drop-in class for handling incoming deeplinks.
 * With the Hoko framework you can map routes to your activities, add handlers that trigger when
 * deeplinks are the point of entry to your application.
 *
 * - Deeplinking - handles every incoming deeplink, so long as it has been mapped
 *
 * You should setup Hoko on your Application's onCreate(...), by calling
 * Hoko.setup(this, "YOUR-API-TOKEN")
 */
public class Hoko {

    public static final String VERSION = "2.0.1";

    // Static Instance
    private static Hoko mInstance;

    // Private modules
    private Deeplinking mDeeplinking;

    // Private variables
    private boolean mDebugMode;

    // Private initializer
    private Hoko(Context context, String token, boolean debugMode) {
        mDebugMode = debugMode;

        Networking.setupNetworking(context);

        mDeeplinking = new Deeplinking(token, context);
    }

    // Setup
    /**
     * Setups all the Hoko module instances, logging and asynchronous networking queues.
     * Setting up with a token will make sure you can take full advantage of the Hoko service,
     * as you will be able to track everything through automatic Analytics, which will
     * be shown on your Hoko dashboards.
     * Also sets the debug mode for the devices set.  Debug mode serves the purpose of uploading
     * the mapped Routes to the Hoko backend service.
     * <pre>{@code
     * Hoko.setup(this, "YOUR-API-TOKEN", true);
     * }</pre>
     *
     * @param context   Your application context.
     * @param token     Hoko service API key.
     * @param testDevices Toggle debug mode manually.
     */
    public static void setup(Context context, String token, String... testDevices) {
        if (mInstance == null) {
            boolean debugMode = debugModeWithTestDevices(context, token, testDevices);
            setVerbose(debugMode);
            mInstance = new Hoko(context, token, debugMode);
            mInstance.checkVersions();
            AnnotationParser.parseActivities(context);

        } else {
            HokoLog.e(new SetupCalledMoreThanOnceException());
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
            HokoLog.e(new SetupNotCalledYetException());
            return null;
        }
        return mInstance.mDeeplinking;
    }

    // Logging

    /**
     * Use this function to enable or disable logging from the Hoko SDK.
     * It is disabled by default.
     *
     * @param verbose true to enable logging, false to disable.
     */
    public static void setVerbose(boolean verbose) {
        HokoLog.setVerbose(verbose);
    }

    // Debug

    /**
     * Returns a boolean on whether the debug mode is activated or not.
     *
     * @return true if debug mode is on, false otherwise.
     */
    public static boolean isDebugMode() {
        if (mInstance == null) {
            HokoLog.e(new SetupNotCalledYetException());
            return false;
        }
        return mInstance.mDebugMode;
    }

    /**
     * This will check for debug mode with the device IDs specified.
     * Will also print a description to help developer integrate easier.
     *
     * @param context     A context object.
     * @param token       The Hoko token to be printed out
     * @param testDevices An array of test devices.
     * @return true if debug mode is active, false otherwise.
     */
    private static boolean debugModeWithTestDevices(Context context, String token, String... testDevices) {
        List<String> testDevicesList = new ArrayList<>(Arrays.asList(testDevices));
        boolean debugMode = testDevicesList.contains(Device.getDeviceID(context));
        if (!debugMode) {
            testDevicesList.add(Device.getDeviceID(context));
            String testDevicesString = Utils.joinComponentsByString(testDevicesList, "\", \"");
            Log.d(HokoLog.TAG, "To upload the mapped routes to Hoko on this device, please " +
                    "make sure to setup the SDK with \nHoko.setup(this, \"" + token +
                    "\", \"" + testDevicesString + "\")");
        }
        return debugMode;
    }

    /**
     * Checks for new SDK version on GITHUB, also checks for which version was previously installed,
     * and in case its different it will reset the routes that were previously posted, to allow new
     * routes to be posted.
     */
    private void checkVersions() {
        if (mDebugMode) {
            new VersionChecker().checkForNewVersion(VERSION);
        }
    }

}
