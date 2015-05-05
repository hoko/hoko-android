package com.hokolinks.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * URL is a wrapper around a Uri object. It provides functions to parse the query parameters,
 * url scheme, component matching and route matching.
 */
public class URL {

    private Uri mUri;

    private String mScheme;
    private HashMap<String, String> mQueryParameters;

    /**
     * The constructor for URL receives a url and parses it into scheme and query parameters.
     *
     * @param urlString A String object representing a url.
     */
    public URL(String urlString) {
        mUri = Uri.parse(sanitizeURL(urlString));
        mScheme = mUri.getScheme();
        mQueryParameters = queryParameters(mUri);
    }

    /**
     * Retrieves the query parameters out of a Uri object.
     *
     * @param uri A Uri object.
     * @return  The query parameters in HashMap form.
     */
    private static HashMap<String, String> queryParameters(Uri uri) {
        HashMap<String, String> queryParameters = new HashMap<>();
        Set<String> queryParameterNames = uri.getQueryParameterNames();
        for (String queryParameterName : queryParameterNames) {
            queryParameters.put(queryParameterName, uri.getQueryParameter(queryParameterName));
        }

        return queryParameters;
    }

    /**
     * Matches path components with route components. This will result in a map between the two.
     *
     * @param pathComponents Path components in list form.
     * @param routeComponents Route components in list form.
     * @return A HashMap where the keys are route components and values are their value
     * representation of path components.
     */
    private static HashMap<String, String> matchComponents(List<String> pathComponents,
                                                           List<String> routeComponents) {
        HashMap<String, String> routeParameters = new HashMap<>();
        for (int index = 0; index < pathComponents.size(); index++) {
            String pathComponent = pathComponents.get(index);
            String routeComponent = routeComponents.get(index);
            if (routeComponent.startsWith(":")) {
                routeParameters.put(routeComponent.substring(1), pathComponent);
            } else if (routeComponent.compareToIgnoreCase(pathComponent) != 0) {
                return null;
            }
        }
        return routeParameters;
    }

    /**
     * Sanitizes a URL String to remove '/' characters.
     *
     * @param urlString A String object representing a url.
     * @return A String object representing the sanitized url.
     */
    public static String sanitizeURL(String urlString) {
        String sanitizedURLString = urlString.replaceAll("^/+", "");
        sanitizedURLString = sanitizedURLString.replaceAll("/+$", "");
        sanitizedURLString = sanitizedURLString.replaceAll("(?<!:)(/)+", "/");
        return sanitizedURLString;
    }

    public String getScheme() {
        return mScheme;
    }

    public HashMap<String, String> getQueryParameters() {
        return mQueryParameters;
    }

    /**
     * Tries to match a Route object to this URL instance.
     * Will perform path components and route components validation and return the matched values
     * in case it is a valid match.
     *
     * @param route A Route instance.
     * @return A HashMap where the keys are route components and values are their value
     * representation of path components.
     */
    public HashMap<String, String> matchesWithRoute(Route route) {
        List<String> pathComponents = new ArrayList<>();
        pathComponents.add(mUri.getAuthority());
        pathComponents.addAll(mUri.getPathSegments());
        List<String> routeComponents = route.getComponents();

        if (routeComponents == null || pathComponents.size() != routeComponents.size())
            return null;
        return matchComponents(pathComponents, routeComponents);
    }
}
