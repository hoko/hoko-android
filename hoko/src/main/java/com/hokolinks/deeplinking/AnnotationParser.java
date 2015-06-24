package com.hokolinks.deeplinking;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.hokolinks.Hoko;
import com.hokolinks.deeplinking.annotations.DeeplinkDefaultRoute;
import com.hokolinks.deeplinking.annotations.DeeplinkFragmentActivity;
import com.hokolinks.deeplinking.annotations.DeeplinkQueryParameter;
import com.hokolinks.deeplinking.annotations.DeeplinkRoute;
import com.hokolinks.deeplinking.annotations.DeeplinkRouteParameter;
import com.hokolinks.model.Deeplink;
import com.hokolinks.model.IntentRouteImpl;
import com.hokolinks.model.Route;
import com.hokolinks.model.exceptions.ActivityNotDeeplinkableException;
import com.hokolinks.utils.log.HokoLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * HokoAnnotation parser serves the purpose of analyzing the Activity classes on the application
 * and determining their deeplinking capabilities based on the given Hoko annotations.
 */
public class AnnotationParser {

    // Route link

    /**
     * Get the route annotated with DeeplinkRoute on a certain class.
     *
     * @param classObject A classObject (usually an activity).
     * @return The route string.
     */
    public static String routeFromClass(Class classObject) {
        DeeplinkRoute annotation = (DeeplinkRoute) classObject
                .getAnnotation(DeeplinkRoute.class);
        if (annotation != null && annotation.value().compareTo(DeeplinkRoute.noValue) != 0) {
            return annotation.value();
        }
        return null;
    }


    // Generating

    /**
     * This generates a Deeplink instance from an Activity instance, taking basis on the
     * annotations within. It collects values, propagates the hashmaps and returns a Deeplink.
     *
     * @param activity An annotated activity.
     * @return A Deeplink instance or null.
     */
    public static Deeplink deeplinkFromActivity(Activity activity) {
        return deeplinkFromObject(activity);
    }

    /**
     * This generates a Deeplink instance from a Fragment instance, taking basis on the
     * annotations within. It collects values, propagates the hashmaps and returns a Deeplink.
     *
     * @param fragment An annotated fragment.
     * @return A Deeplink instance or null.
     */
    public static Deeplink deeplinkFromFragment(Fragment fragment) {
        return deeplinkFromObject(fragment);
    }

    private static Deeplink deeplinkFromObject(Object object) {
        String route = routeFromClass(object.getClass());
        if (route != null) {
            HashMap<String, String> routeParameters = getRouteParametersFromInstance(object);
            HashMap<String, String> queryParameters = getQueryParametersFromInstance(object);
            if (routeParameters == null) {
                HokoLog.e(new ActivityNotDeeplinkableException(object.getClass().getName()));
                return null;
            }
            return Deeplink.deeplink(route, routeParameters, queryParameters);
        } else {
            HokoLog.e(new ActivityNotDeeplinkableException(object.getClass().getName()));
        }
        return null;
    }

    /**
     * Returns instantiated route parameters from an instantiated activity or fragment object.
     * Retrieves all fields annotated with the DeeplinkRouteParameter annotations along with their
     * values. Will return null if one value is either null or could not be retrieved.
     *
     * @param object An annotated object (either an Activity or a Fragment).
     * @return A HashMap with route components as keys and route parameters as values.
     */
    private static HashMap<String, String> getRouteParametersFromInstance(Object object) {
        HashMap<String, Field> routeParameterFields = getRouteParameters(object.getClass());
        HashMap<String, String> routeParameters = new HashMap<>();
        for (String key : routeParameterFields.keySet()) {
            Field field = routeParameterFields.get(key);
            String value = getValueForField(field, object);
            if (value != null) {
                routeParameters.put(key, value);
            } else {
                return null;
            }
        }
        return routeParameters;
    }

    /**
     * Returns instantiated query parameters from an instantiated activity or fragment object.
     * Retrieves all fields annotated with the DeeplinkQueryParameter annotations along with their
     * values. Will always return because query parameters are not required.
     *
     * @param object An annotated object (either an Activity or a Fragment).
     * @return A HashMap with query components as keys and query parameters as values.
     */
    private static HashMap<String, String> getQueryParametersFromInstance(Object object) {
        HashMap<String, Field> queryParameterFields = getQueryParameters(object.getClass());
        HashMap<String, String> queryParameters = new HashMap<>();
        for (String key : queryParameterFields.keySet()) {
            Field field = queryParameterFields.get(key);
            String value = getValueForField(field, object);
            if (value != null) {
                queryParameters.put(key, value);
            }
        }
        return queryParameters;
    }

    // Injecting

    /**
     * Injects route parameters on to the fields of an activity instance, from an inbound intent.
     * Uses a route to map parameters to keys and then to the correct fields on the activity
     * object.
     *
     * @param activity An annotated activity.
     * @return true in case it injected values, false otherwise.
     */
    public static boolean inject(Activity activity) {
        String route = activity.getIntent().getStringExtra(IntentRouteImpl.HokoRouteBundleKey);
        if (route == null)
            return false;

        Bundle routeParametersBundle = activity.getIntent()
                .getBundleExtra(IntentRouteImpl.HokoRouteRouteParametersBundleKey);
        Bundle queryParametersBundle = activity.getIntent()
                .getBundleExtra(IntentRouteImpl.HokoRouteQueryParametersBundleKey);

        String routeFromClass = routeFromClass(activity.getClass());

        if (routeFromClass != null && routeFromClass.compareTo(route) == 0) { // Activity
            return inject(activity, route, routeParametersBundle, queryParametersBundle);
        } else if (activity instanceof FragmentActivity) {
            return injectFragment((FragmentActivity) activity, route, routeParametersBundle,
                    queryParametersBundle);
        }
        return false;
    }

    /**
     * Injects route parameters on to the fields of an fragment instance, from inbound arguments.
     * Uses a route to map parameters to keys and then to the correct fields on the fragment
     * object.
     *
     * @param fragment An annotated fragment.
     * @return true in case it injected values, false otherwise.
     */
    public static boolean inject(Fragment fragment) {
        if (fragment.getArguments() == null)
            return false;

        String route = fragment.getArguments().getString(IntentRouteImpl.HokoRouteBundleKey);
        if (route == null)
            return false;

        Bundle routeParametersBundle = fragment.getArguments()
                .getBundle(IntentRouteImpl.HokoRouteRouteParametersBundleKey);
        Bundle queryParametersBundle = fragment.getArguments()
                .getBundle(IntentRouteImpl.HokoRouteQueryParametersBundleKey);

        return inject(fragment, route, routeParametersBundle, queryParametersBundle);
    }

    /**
     * Injects route parameters on to the fields of an fragment instance, from inbound arguments.
     * Uses a route to map parameters to keys and then to the correct fields on the fragment
     * object.
     *
     * @param fragment An annotated fragment.
     * @return true in case it injected values, false otherwise.
     */
    @TargetApi(11)
    public static boolean inject(android.app.Fragment fragment) {
        if (fragment.getArguments() == null)
            return false;

        String route = fragment.getArguments().getString(IntentRouteImpl.HokoRouteBundleKey);
        if (route == null)
            return false;

        Bundle routeParametersBundle = fragment.getArguments()
                .getBundle(IntentRouteImpl.HokoRouteRouteParametersBundleKey);
        Bundle queryParametersBundle = fragment.getArguments()
                .getBundle(IntentRouteImpl.HokoRouteQueryParametersBundleKey);

        return inject(fragment, route, routeParametersBundle, queryParametersBundle);
    }

    /**
     * Injects a FragmentActivity with a possible deeplinkable fragment, its route parameters and
     * its query parameters.
     *
     * @param activity              A FragmentActivity object.
     * @param route                 A route string.
     * @param routeParametersBundle A bundle containing the route parameters.
     * @param queryParametersBundle A bundle containing the query parameters.
     * @return true in case it injected values, false otherwise.
     */
    private static boolean injectFragment(FragmentActivity activity, String route,
                                          Bundle routeParametersBundle,
                                          Bundle queryParametersBundle) {
        DeeplinkFragmentActivity deeplinkFragmentActivityAnnotation =
                getFragmentAnnotationFromClass(activity.getClass());

        if (deeplinkFragmentActivityAnnotation == null
                || deeplinkFragmentActivityAnnotation.fragments().length == 0)
            return false;

        Class[] fragmentClasses = deeplinkFragmentActivityAnnotation.fragments();
        Class<?> fragmentClass = findFragmentForRoute(route, fragmentClasses);

        if (fragmentClass == null)
            return false;

        try {
            Bundle bundle = new Bundle();
            bundle.putString(IntentRouteImpl.HokoRouteBundleKey, route);
            bundle.putBundle(IntentRouteImpl.HokoRouteRouteParametersBundleKey, routeParametersBundle);
            bundle.putBundle(IntentRouteImpl.HokoRouteQueryParametersBundleKey, queryParametersBundle);
            if (Fragment.class.isAssignableFrom(fragmentClass)) {
                Fragment fragment = (Fragment) fragmentClass.getDeclaredConstructor().newInstance();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(deeplinkFragmentActivityAnnotation.id(), fragment).commit();
                return true;
            } else if (android.app.Fragment.class.isAssignableFrom(fragmentClass)) {
                android.app.Fragment fragment = (android.app.Fragment) fragmentClass
                        .getDeclaredConstructor().newInstance();
                fragment.setArguments(bundle);
                android.app.FragmentManager fragmentManager = activity.getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(deeplinkFragmentActivityAnnotation.id(), fragment).commit();
                return true;
            }
        } catch (Exception e) {
            HokoLog.e(e);
        }

        return false;
    }

    /**
     * Injects a route, route parameters and query parameters to a given annotated object.
     *
     * @param object                The annotated object.
     * @param route                 The given route.
     * @param routeParametersBundle A bundle containing the route parameters.
     * @param queryParametersBundle A bundle containing the query parameters.
     * @return true in case it injected values, false otherwise.
     */
    private static boolean inject(Object object, String route, Bundle routeParametersBundle,
                                  Bundle queryParametersBundle) {
        Deeplinking deeplinking = Hoko.deeplinking();
        if (deeplinking != null) {
            Route routeObj = deeplinking.routing().getRoute(route);
            if (routeObj instanceof IntentRouteImpl) {
                IntentRouteImpl hokoIntentRoute = (IntentRouteImpl) routeObj;
                if (routeParametersBundle != null && queryParametersBundle != null) {
                    if (hokoIntentRoute.getRouteParameters() != null) {
                        for (String key : hokoIntentRoute.getRouteParameters().keySet()) {
                            Field field = hokoIntentRoute.getRouteParameters().get(key);
                            String parameter = routeParametersBundle.getString(key);
                            if (!setValueForField(field, object, parameter, true))
                                return false;
                        }
                    }

                    if (hokoIntentRoute.getQueryParameters() != null) {
                        for (String key : hokoIntentRoute.getQueryParameters().keySet()) {
                            Field field = hokoIntentRoute.getQueryParameters().get(key);
                            String parameter = queryParametersBundle.getString(key);
                            setValueForField(field, object, parameter, false);
                        }
                    }

                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Sets values to an object's field independently of access modifiers.
     * It also abstracts the fields actual class or primitive type, performing type-safe casts and
     * conversions.
     *
     * @param field    The field to be set.
     * @param object   The object on which the field should be set
     * @param value    The String value to set on to the field.
     * @param logError true if an error should be logged, false otherwise
     * @return true if the value was properly set, false otherwise.
     */
    private static boolean setValueForField(Field field, Object object, String value,
                                            boolean logError) {
        boolean accessible = field.isAccessible();
        boolean returnValue;
        try {
            field.setAccessible(true);
            Class<?> classObject = field.getType();
            if (field.getType().isPrimitive()) {
                if (classObject.equals(int.class)) {
                    field.setInt(object, Integer.parseInt(value));
                } else if (classObject.equals(float.class)) {
                    field.setFloat(object, Float.parseFloat(value));
                } else if (classObject.equals(double.class)) {
                    field.setDouble(object, Double.parseDouble(value));
                } else if (classObject.equals(short.class)) {
                    field.setShort(object, Short.parseShort(value));
                } else if (classObject.equals(boolean.class)) {
                    field.setBoolean(object, Boolean.parseBoolean(value));
                } else if (classObject.equals(byte.class)) {
                    field.setByte(object, Byte.parseByte(value));
                } else if (classObject.equals(char.class)) {
                    field.setChar(object, value.charAt(0));
                }
            } else {
                if (classObject.equals(String.class)) {
                    field.set(object, value);
                } else if (classObject.equals(Integer.class)) {
                    field.set(object, Integer.valueOf(value));
                } else if (classObject.equals(Float.class)) {
                    field.set(object, Float.valueOf(value));
                } else if (classObject.equals(Double.class)) {
                    field.set(object, Double.valueOf(value));
                } else if (classObject.equals(Short.class)) {
                    field.set(object, Short.valueOf(value));
                } else if (classObject.equals(Boolean.class)) {
                    field.set(object, Boolean.valueOf(value));
                } else if (classObject.equals(Byte.class)) {
                    field.set(object, Byte.valueOf(value));
                } else if (classObject.equals(Character.class)) {
                    field.set(object, value.charAt(0));
                }
            }
            returnValue = true;
        } catch (IllegalAccessException e) {
            if (logError)
                HokoLog.e(e);
            returnValue = false;
        } finally {
            field.setAccessible(accessible);
        }
        return returnValue;
    }

    /**
     * Returns the String value of a certain field on a certain object, independent of access
     * modifiers.
     * It also provides type-safety by performing casting and conversion from primitive-types and
     * supported classes.
     *
     * @param field  The field where the value will be extracted from.
     * @param object The object on which the field should be extracted.
     * @return The value extracted from the object's field.
     */
    public static String getValueForField(Field field, Object object) {
        boolean accessible = field.isAccessible();
        String returnValue = null;
        try {
            field.setAccessible(true);
            Class<?> classObject = field.getType();
            if (field.getType().isPrimitive()) {
                if (classObject.equals(int.class)) {
                    returnValue = String.valueOf(field.getInt(object));
                } else if (classObject.equals(float.class)) {
                    returnValue = String.valueOf(field.getFloat(object));
                } else if (classObject.equals(double.class)) {
                    returnValue = String.valueOf(field.getDouble(object));
                } else if (classObject.equals(short.class)) {
                    returnValue = String.valueOf(field.getShort(object));
                } else if (classObject.equals(boolean.class)) {
                    returnValue = String.valueOf(field.getBoolean(object));
                } else if (classObject.equals(byte.class)) {
                    returnValue = String.valueOf(field.getByte(object));
                } else if (classObject.equals(char.class)) {
                    returnValue = String.valueOf(field.getChar(object));
                }
            } else {
                if (classObject.equals(String.class)) {
                    returnValue = (String) field.get(object);
                } else if (classObject.equals(Integer.class)) {
                    returnValue = field.get(object).toString();
                } else if (classObject.equals(Float.class)) {
                    returnValue = field.get(object).toString();
                } else if (classObject.equals(Double.class)) {
                    returnValue = field.get(object).toString();
                } else if (classObject.equals(Short.class)) {
                    returnValue = field.get(object).toString();
                } else if (classObject.equals(Boolean.class)) {
                    returnValue = field.get(object).toString();
                } else if (classObject.equals(Byte.class)) {
                    returnValue = field.get(object).toString();
                } else if (classObject.equals(Character.class)) {
                    returnValue = field.get(object).toString();
                }
            }
        } catch (IllegalAccessException e) {
            HokoLog.e(e);
            returnValue = null;
        } finally {
            field.setAccessible(accessible);
        }
        return returnValue;
    }

    /**
     * Check if a certain class is the default route, by checking if it has the DeeplinkDefaultRoute
     * annotation.
     *
     * @param classObject A classObject (usually an activity or a fragment).
     * @return true in case it has the DeeplinkDefaultRoute annotation.
     */
    public static boolean isDefaultRoute(Class classObject) {
        DeeplinkDefaultRoute annotation =
                (DeeplinkDefaultRoute) classObject.getAnnotation(DeeplinkDefaultRoute.class);
        return annotation != null;
    }

    /**
     * This function will parse all activities extracted from the AndroidManifest.xml, retrieving
     * the route format, the activity name, its annotated routeParameters and queryParameters, and
     * finally mapping them to the Deeplinking module according to DeeplinkRoute or
     * DeeplinkDefaultRoute annotations.
     *
     * @param context The application context.
     */
    public static void parseActivities(Context context) {
        List<String> activitiesList = getActivities(context);
        for (String activityName : activitiesList) {
            try {
                Class classObject = Class.forName(activityName);
                mapClassToDeeplink(activityName, classObject, true, true);
            } catch (ClassNotFoundException e) {
                HokoLog.e(e);
            }
        }
    }

    /**
     * This function will parse all the fragment annotations in a given class object and will map
     * those deeplinks to the parent activity.
     *
     * @param classObject  A classObject (usually an activity).
     * @param activityName The activityName.
     */
    private static void parseFragmentActivity(Class classObject, String activityName) {
        DeeplinkFragmentActivity deeplinkFragmentActivityAnnotation =
                getFragmentAnnotationFromClass(classObject);
        if (deeplinkFragmentActivityAnnotation != null) {
            Class[] fragmentClasses = deeplinkFragmentActivityAnnotation.fragments();
            for (Class fragmentClass : fragmentClasses) {
                mapClassToDeeplink(activityName, fragmentClass, false, false);
            }
        }
    }

    /**
     * Maps a given class and activity to a deeplinking route.
     *
     * @param activityName   The activity class name.
     * @param classObject    The class object.
     * @param shouldDefault  true if it should look for a default route, false otherwise.
     * @param shouldFragment true if it should look for fragments inside the class, false otherwise.
     */
    private static void mapClassToDeeplink(String activityName, Class classObject,
                                           boolean shouldDefault, boolean shouldFragment) {
        String route = routeFromClass(classObject);
        Deeplinking deeplinking = Hoko.deeplinking();
        if (deeplinking != null) {
            if (route != null) {
                HashMap<String, Field> routeParameters = getRouteParameters(classObject);
                HashMap<String, Field> queryParameters = getQueryParameters(classObject);
                deeplinking.mapRoute(route, activityName, routeParameters, queryParameters);
            }
            if (shouldDefault && isDefaultRoute(classObject)) {
                HashMap<String, Field> queryParameters = getQueryParameters(classObject);
                deeplinking.mapDefaultRoute(activityName, queryParameters);
            }
        }

        if (shouldFragment) {
            parseFragmentActivity(classObject, activityName);
        }
    }

    /**
     * Retrieves the DeeplinkFragmentActivity annotation from a given class.
     *
     * @param classObject The class object.
     * @return The DeeplinkFragmentActivity annotation found, null otherwise.
     */
    private static DeeplinkFragmentActivity getFragmentAnnotationFromClass(Class classObject) {
        return (DeeplinkFragmentActivity) classObject.getAnnotation(DeeplinkFragmentActivity.class);
    }

    /**
     * Finds the fragment which matches to a given route object.
     *
     * @param route   A route string.
     * @param classes An array of Classes.
     * @return The Class that matches the route.
     */
    private static Class findFragmentForRoute(String route, Class[] classes) {
        for (Class classObject : classes) {
            String routeFromClass = routeFromClass(classObject);
            if (routeFromClass != null && routeFromClass.compareTo(route) == 0)
                return classObject;
        }
        return null;
    }

    /**
     * Retrieves the fields annotated with DeeplinkRouteParameter annotation from a given class.
     *
     * @param classObject A classObject (usually an activity).
     * @return A HashMap with the route component as key and the Field as value.
     */
    private static HashMap<String, Field> getRouteParameters(Class classObject) {
        HashMap<String, Field> routeParametersMap = new HashMap<>();
        List<Field> fieldList = getFields(classObject);
        for (Field field : fieldList) {
            DeeplinkRouteParameter routeParameterAnnotation =
                    field.getAnnotation(DeeplinkRouteParameter.class);
            if (routeParameterAnnotation != null) {
                routeParametersMap.put(routeParameterAnnotation.value(), field);
            }
        }
        return routeParametersMap;
    }

    /**
     * Retrieves the fields annotated with DeeplinkQueryParameter annotation from a given class.
     *
     * @param classObject A classObject (usually an activity).
     * @return A HashMap with the query component as key and the Field as value.
     */
    private static HashMap<String, Field> getQueryParameters(Class classObject) {
        HashMap<String, Field> queryParametersMap = new HashMap<>();
        List<Field> fieldList = getFields(classObject);
        for (Field field : fieldList) {
            DeeplinkQueryParameter queryParameterAnnotation =
                    field.getAnnotation(DeeplinkQueryParameter.class);
            if (queryParameterAnnotation != null) {
                queryParametersMap.put(queryParameterAnnotation.value(), field);
            }
        }
        return queryParametersMap;
    }

    /**
     * Retrieves all the fields from a given class.
     *
     * @param classObject A classObject (usually an activity).
     * @return A list of the fields on a given class.
     */
    public static List<Field> getFields(Class classObject) {
        return new ArrayList<>(Arrays.asList(classObject.getDeclaredFields()));
    }

    /**
     * Retrieves all the Activities declared in the AndroidManifest.xml file.
     *
     * @param context The application context.
     * @return A list of the class names for the available activities.
     */
    private static List<String> getActivities(Context context) {
        List<String> activitiesList = new ArrayList<>();
        try {
            ActivityInfo[] activityInfoList = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES)
                    .activities;
            for (ActivityInfo activityInfo : activityInfoList) {
                activitiesList.add(activityInfo.name);
            }
        } catch (Exception e) {
            HokoLog.e(e);
        }
        return activitiesList;
    }

}
