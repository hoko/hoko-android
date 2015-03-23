package com.hokolinks.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.hokolinks.utils.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * Utils serves many purposes, but mostly loading and saving objects/strings to file or
 * to the SharedPreferences. It also provides utility methods to generate random UUIDs and
 * sanitizing routes.
 */
public class Utils {

    // Hoko folder name
    private static final String HokoUtilsFolderName = "hoko";
    // Hoko SharedPreferences key
    private static final String HokoUtilsSharedPreferencesKey = "com.hoko";

    /**
     * Checks where the application has a given permission granted on the AndroidManifest.xml file.
     *
     * @param permission The permission to be checked.
     * @param context    A context object.
     * @return true if has permission, false otherwise.
     */
    public static boolean hasPermission(String permission, Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager.checkPermission(permission, context.getPackageName())
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                Log.e("Requesting permission " + permission
                        + " but it is not on the AndroidManifest.xml");
                return false;
            }
        } catch (Exception e) {
            Log.e(e);
            return false;
        }
    }

    /**
     * Saves a string to the SharedPreferences with a given key.
     *
     * @param string  The string to be saved.
     * @param key     The key associated to the string value.
     * @param context A context object.
     */
    public static void saveString(String string, String key, Context context) {
        try {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(HokoUtilsSharedPreferencesKey,
                            Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, string);
            editor.apply();
        } catch (NullPointerException e) {
            Log.e(e);
        }
    }

    /**
     * Loads a string from the SharedPreferences with a given key.
     *
     * @param key     The key associated to the string value.
     * @param context A context object.
     * @return The string in case it exists, null otherwise.
     */
    public static String getString(String key, Context context) {
        try {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(HokoUtilsSharedPreferencesKey,
                            Context.MODE_PRIVATE);
            return sharedPreferences.getString(key, null);
        } catch (NullPointerException e) {
            Log.e(e);
            return null;
        }
    }

    /**
     * Saves an object to the private filesystem of the application.
     *
     * @param object   The object to be saved, needs to implement Serializable.
     * @param filename The filename with which it should be saved.
     * @param context  A context object.
     */
    public static void saveToFile(Object object, String filename, Context context) {
        try {
            FileOutputStream fileOutputStream =
                    new FileOutputStream(fileFromFilename(filename, context));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
        } catch (IOException e) {
            Log.e(e);
        } catch (NullPointerException e) {
            Log.e(e);
        }
    }

    /**
     * Loads an object from the private filesystem of the application.
     *
     * @param filename The filename from which it should be loaded.
     * @param context  A context object.
     * @return The object that was previously saved, or null.
     */
    public static Object loadFromFile(String filename, Context context) {
        try {
            FileInputStream fileInputStream =
                    new FileInputStream(fileFromFilename(filename, context));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            return object;
        } catch (IOException e) {
            Log.e(e);
        } catch (ClassNotFoundException e) {
            Log.e(e);
        } catch (NullPointerException e) {
            Log.e(e);
        }
        return null;
    }

    /**
     * Generates a random UUID string adding a time factor to guarantee some more randomness.
     *
     * @return A random UUID string.
     */
    public static String generateUUID() {
        String uid = UUID.randomUUID().toString().toUpperCase(Locale.ENGLISH);
        long timeSince1970 = Calendar.getInstance().getTimeInMillis();
        return uid + "-" + String.valueOf(timeSince1970).substring(0, 10);
    }

    /**
     * Sanitizes a route by replacing leading and ending '/' characters.
     *
     * @param route The route string.
     * @return The sanitized route string.
     */
    public static String sanitizeRoute(String route) {
        String sanitizedRoute = route.replaceAll("^/+", "");
        sanitizedRoute = sanitizedRoute.replaceAll("/+$", "");
        return sanitizedRoute;
    }

    /**
     * Helper function to return the File from a given filename.
     *
     * @param filename The filename corresponding to the File.
     * @param context  A context object.
     * @return The File.
     */
    private static File fileFromFilename(String filename, Context context) {
        File directory = context.getDir(HokoUtilsFolderName, Context.MODE_PRIVATE);
        return new File(directory, filename);
    }

    /**
     * Calculates the MD5 value of a given String object.
     *
     * @param string The string of which to get the MD5 value.
     * @return The MD5 value.
     */
    public static String md5FromString(String string) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(string.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }


}
