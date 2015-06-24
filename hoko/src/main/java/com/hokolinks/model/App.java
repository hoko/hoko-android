package com.hokolinks.model;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.hokolinks.utils.log.HokoLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * App is a helper class to get all the necessary information of the Application environment.
 */
public class App {

    private static final String ENVIRONMENT_DEBUG = "debug";
    private static final String ENVIRONMENT_RELEASE = "release";

    /**
     * Returns the name of the application Hoko is being run on.
     *
     * @param context A context object.
     * @return The name of the application.
     */
    public static String getName(Context context) {
        try {
            int stringId = context.getApplicationInfo().labelRes;
            return context.getString(stringId);
        } catch (NullPointerException e) {
            HokoLog.e(e);
            return null;
        }
    }

    /**
     * Returns the package name of the application Hoko is being run on.
     *
     * @param context A context object.
     * @return The package name of the application.
     */
    public static String getPackageName(Context context) {
        try {
            return context.getPackageName();
        } catch (NullPointerException e) {
            HokoLog.e(e);
            return null;
        }
    }

    /**
     * Returns the version of the application Hoko is being run on.
     *
     * @param context A context object.
     * @return The version of the application.
     */
    public static String getVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception exception) {
            HokoLog.e(exception);
        }
        return null;

    }

    /**
     * Returns the version code of the application Hoko is being run on.
     *
     * @param context A context object.
     * @return The version code of the application.
     */
    public static String getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return String.valueOf(packageInfo.versionCode);
        } catch (Exception exception) {
            HokoLog.e(exception);
        }
        return null;
    }

    /**
     * Converts all the App information into a JSONObject to be sent to the Hoko backend
     * service.
     *
     * @param context A context object.
     * @return The JSONObject representation of App.
     */
    public static JSONObject json(Context context) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("name", getName(context));
            jsonObject.putOpt("bundle", getPackageName(context));
            jsonObject.putOpt("version", getVersion(context));
            jsonObject.putOpt("build", getVersionCode(context));
            return jsonObject;
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return null;
    }

    /**
     * Check whether the application is running in debug mode by checking the application's
     * BuildConfig static fields. This is done through reflection to avoid manually requesting the
     * developer to provide either the BuildConfig class or the actual debug variable.
     * Will default to a false debug mode in case it can't find the class or the field.
     *
     * @param context A context object.
     * @return true if the application is in debug mode, false otherwise.
     */
    public static boolean isDebug(Context context) {
        try {
            Class buildConfigClass = getBuildConfigClass(context);
            if (buildConfigClass != null) {
                Field debugField = buildConfigClass.getDeclaredField("DEBUG");
                return debugField.getBoolean(null);
            }
        } catch (Exception e) {
            HokoLog.e(e);
        }
        return false;
    }

    /**
     * Returns the environment with regards of finding DEBUG inside a BuildConfig.
     *
     * @param context A context object.
     * @return The environment string.
     */
    public static String getEnvironment(Context context) {
        return isDebug(context) ? ENVIRONMENT_DEBUG : ENVIRONMENT_RELEASE;
    }

    /**
     * Retrieves the BuildConfig class from a given context.
     * Will try to get it from the context's package name, otherwise it will go through the
     * package hierarchy trying to find a BuildConfig class.
     *
     * @param context A context object.
     * @return A Class object or null.
     */
    private static Class<?> getBuildConfigClass(Context context) {
        Class<?> klass = getBuildConfigClassFromPackage(context.getPackageName(), false);
        if (klass == null) {
            klass = getBuildConfigClassFromPackage(context.getClass().getPackage().getName(), true);
        }
        return klass;
    }

    /**
     * Retrieves the BuildConfig class from a package name.
     * Will try to get it from the supplied package name, if the class is not found, depending on
     * the traverse value it will try to recursively find the class on the package hierarchy.
     *
     * @param packageName The package name.
     * @param traverse    true if it should traver the package hierarchy, false otherwise.
     * @return A Class object or null.
     */
    private static Class<?> getBuildConfigClassFromPackage(String packageName, boolean traverse) {
        try {
            return Class.forName(packageName + ".BuildConfig");
        } catch (ClassNotFoundException e) {
            if (traverse) {
                int indexOfLastDot = packageName.lastIndexOf('.');
                if (indexOfLastDot != -1) {
                    return getBuildConfigClassFromPackage(packageName.substring(0, indexOfLastDot),
                            true);
                }
            }
            return null;
        }
    }
}
