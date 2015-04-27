package com.hokolinks.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.hokolinks.utils.Utils;
import com.hokolinks.utils.log.HokoLog;
import com.hokolinks.utils.networking.Networking;
import com.hokolinks.utils.networking.async.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * App is a helper class to get all the necessary information of the Application environment.
 */
public class App {

    private static final String HokoAppIconKey = "HokoAppIconKey";
    private static final String HokoAppEnvironmentDebug = "debug";
    private static final String HokoAppEnvironmentRelease = "release";

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
     * Returns the icon resource id of the application Hoko is being run on.
     *
     * @param context A context object.
     * @return The icon resource id of the application.
     */
    public static int getIcon(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            ApplicationInfo appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
            return appInfo.icon;
        } catch (Exception exception) {
            return android.R.drawable.sym_def_app_icon;
        }
    }

    /**
     * Returns the icon the maximum density possible in Drawable form.
     *
     * @param context A context object.
     * @return The icon drawable.
     */
    @TargetApi(15)
    public static Drawable getIconDrawable(Context context) {
        try {
            int iconResId = getIcon(context);
            Drawable drawable;
            //Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
            if (android.os.Build.VERSION.SDK_INT >= 15) {
                ArrayList<Integer> densities = new ArrayList<Integer>(
                        Arrays.asList(DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_HIGH,
                                DisplayMetrics.DENSITY_MEDIUM, DisplayMetrics.DENSITY_LOW));
                //Build.VERSION_CODES.JELLY_BEAN
                if (Build.VERSION.SDK_INT >= 16) {
                    //DisplayMetrics.DENSITY_XXHIGH
                    densities.add(0, 480);
                }
                //Build.VERSION_CODES.JELLY_BEAN_MR2
                if (Build.VERSION.SDK_INT >= 18) {
                    //DisplayMetrics.DENSITY_XXXHIGH
                    densities.add(0, 640);
                }
                for (int density : densities) {
                    drawable = context.getResources().getDrawableForDensity(iconResId, density);
                    if (drawable != null)
                        return drawable;
                }
            }
            drawable = context.getResources().getDrawable(iconResId);
            return drawable;
        } catch (Exception e) {
            HokoLog.e(e);
            return null;
        }
    }

    @TargetApi(15)
    public static Bitmap getIconBitmapForNotification(Context context) {
        Bitmap iconBitmap = BitmapFactory.decodeResource(context.getResources(), getIcon(context));
        return Bitmap.createScaledBitmap(iconBitmap, context.getResources().getDimensionPixelOffset(android.R.dimen.notification_large_icon_width), context.getResources().getDimensionPixelOffset(android.R.dimen.notification_large_icon_height), true);
    }

    /**
     * Returns the icon of the application in Base64 format.
     *
     * @param context A context object.
     * @return The icon in Base64.
     */
    public static String getBase64Icon(Context context) {
        Drawable iconDrawable = getIconDrawable(context);
        if (iconDrawable != null) {
            BitmapDrawable iconBitmapDrawable = ((BitmapDrawable) iconDrawable);
            Bitmap bitmap = iconBitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            byte[] bitmapByte = stream.toByteArray();

            return Base64.encodeToString(bitmapByte, 0);
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
     * Converts the icon information into a JSONObject to be sent to the Hoko backend service.
     *
     * @param context A context object.
     * @return The JSONObject representation of the application icon in base64.
     */
    public static JSONObject iconJSON(Context context) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("icon", getBase64Icon(context));
            return jsonObject;
        } catch (JSONException e) {
            HokoLog.e(e);
        }
        return null;
    }

    /**
     * This function serves the purpose of upload the application's icon to the Hoko backend
     * service. Will only post if the current icon was not previously posted.
     *
     * @param token   The Hoko API Token.
     * @param context A context object.
     */
    public static void postIcon(String token, Context context) {
        String previousAppIcon = Utils.getString(HokoAppIconKey, context);
        String iconJson = iconJSON(context).toString();
        String iconJsonMD5 = Utils.md5FromString(iconJson);

        if (previousAppIcon == null || previousAppIcon.compareTo(iconJsonMD5) != 0) {
            Utils.saveString(iconJsonMD5, HokoAppIconKey, context);
            Networking.getNetworking().addRequest(new HttpRequest(HttpRequest
                    .HokoNetworkOperationType.POST, "icons", token, iconJson));
        }
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
            String buildConfigClassName = context.getPackageName() + ".BuildConfig";
            Class buildConfigClass = Class.forName(buildConfigClassName);
            Field debugField = buildConfigClass.getDeclaredField("DEBUG");
            return debugField.getBoolean(null);
        } catch (Exception e) {
            HokoLog.e(e);
        }
        return false;
    }

    public static String getEnvironment(Context context) {
        return isDebug(context) ? HokoAppEnvironmentDebug : HokoAppEnvironmentRelease;
    }

}
